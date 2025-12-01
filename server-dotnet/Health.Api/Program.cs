using Microsoft.Data.Sqlite;
using Microsoft.Extensions.FileProviders;
 

var builder = WebApplication.CreateBuilder(args);
var config = builder.Configuration;
var app = builder.Build();

var address = Environment.GetEnvironmentVariable("ADDRESS") ?? config["Server:Address"] ?? "0.0.0.0";
var port = Environment.GetEnvironmentVariable("PORT") ?? config["Server:Port"] ?? "3000";
var dataDir = Path.Combine(Directory.GetCurrentDirectory(), "data");
Directory.CreateDirectory(dataDir);
var dbPath = Environment.GetEnvironmentVariable("DATABASE_PATH") ?? config["Database:Path"] ?? Path.Combine(dataDir, "health.db");
var users = config.GetSection("Users").Get<string[]>() ?? Array.Empty<string>();
var staticPath = Path.Combine(app.Environment.ContentRootPath, "wwwroot");

using (var conn = new SqliteConnection($"Data Source={dbPath}"))
{
    conn.Open();
    using var cmd = conn.CreateCommand();
    cmd.CommandText = "CREATE TABLE IF NOT EXISTS records (id INTEGER PRIMARY KEY AUTOINCREMENT, date TEXT NOT NULL, systolic INTEGER NOT NULL, diastolic INTEGER NOT NULL, pulse INTEGER, name TEXT)";
    cmd.ExecuteNonQuery();

    cmd.CommandText = "PRAGMA table_info(records)";
    var hasName = false;
    using (var ti = cmd.ExecuteReader())
    {
        while (ti.Read())
        {
            if (ti.GetString(1).Equals("name", StringComparison.OrdinalIgnoreCase)) { hasName = true; break; }
        }
    }
    if (!hasName)
    {
        cmd.CommandText = "ALTER TABLE records ADD COLUMN name TEXT";
        cmd.ExecuteNonQuery();
    }
}

app.MapGet("/api/records", (string? start, string? end, string? name, int? page, int? pageSize) =>
{
    using var conn = new SqliteConnection($"Data Source={dbPath}");
    conn.Open();
    var where = new List<string>();
    var p = page.HasValue && page.Value > 0 ? page.Value : 1;
    var ps = pageSize.HasValue && pageSize.Value > 0 ? Math.Min(pageSize.Value, 200) : 20;
    using var countCmd = conn.CreateCommand();
    countCmd.CommandText = "SELECT COUNT(*) FROM records";
    if (!string.IsNullOrEmpty(start)) { where.Add("date >= $start"); countCmd.Parameters.AddWithValue("$start", start); }
    if (!string.IsNullOrEmpty(end)) { where.Add("date <= $end"); countCmd.Parameters.AddWithValue("$end", end); }
    if (!string.IsNullOrEmpty(name)) { where.Add("name = $name"); countCmd.Parameters.AddWithValue("$name", name); }
    if (where.Count > 0) countCmd.CommandText += " WHERE " + string.Join(" AND ", where);
    var total = (long)(countCmd.ExecuteScalar() ?? 0L);

    using var cmd = conn.CreateCommand();
    cmd.CommandText = "SELECT id, date, systolic, diastolic, pulse, name FROM records";
    if (where.Count > 0) cmd.CommandText += " WHERE " + string.Join(" AND ", where);
    cmd.CommandText += " ORDER BY date DESC LIMIT $limit OFFSET $offset";
    cmd.Parameters.AddWithValue("$limit", ps);
    cmd.Parameters.AddWithValue("$offset", (p - 1) * ps);
    if (!string.IsNullOrEmpty(start)) cmd.Parameters.AddWithValue("$start", start);
    if (!string.IsNullOrEmpty(end)) cmd.Parameters.AddWithValue("$end", end);
    if (!string.IsNullOrEmpty(name)) cmd.Parameters.AddWithValue("$name", name);
    using var reader = cmd.ExecuteReader();
    var list = new List<object>();
    while (reader.Read())
    {
        list.Add(new
        {
            id = reader.GetInt64(0),
            date = reader.GetString(1),
            systolic = reader.GetInt32(2),
            diastolic = reader.GetInt32(3),
            pulse = reader.IsDBNull(4) ? (int?)null : reader.GetInt32(4),
            name = reader.IsDBNull(5) ? null : reader.GetString(5)
        });
    }
    var totalPages = (int)Math.Ceiling(total / (double)ps);
    return Results.Json(new { data = list, meta = new { total, page = p, pageSize = ps, totalPages } });
});

app.MapPost("/api/records", async (HttpContext ctx) =>
{
    using var sr = new StreamReader(ctx.Request.Body);
    var body = await sr.ReadToEndAsync();
    var json = System.Text.Json.JsonDocument.Parse(body).RootElement;
    if (!json.TryGetProperty("systolic", out var s) || !json.TryGetProperty("diastolic", out var d))
        return Results.BadRequest(new { error = "收缩压和舒张压为必填项" });
    if (!json.TryGetProperty("name", out var n) || n.ValueKind != System.Text.Json.JsonValueKind.String)
        return Results.BadRequest(new { error = "姓名为必填项" });
    var pulse = json.TryGetProperty("pulse", out var p) && p.ValueKind != System.Text.Json.JsonValueKind.Null ? p.GetInt32() : (int?)null;
    var date = DateTime.UtcNow.ToString("o");
    using var conn = new SqliteConnection($"Data Source={dbPath}");
    conn.Open();
    using var cmd = conn.CreateCommand();
    cmd.CommandText = "INSERT INTO records (date, systolic, diastolic, pulse, name) VALUES ($date, $sys, $dia, $pulse, $name); SELECT last_insert_rowid();";
    cmd.Parameters.AddWithValue("$date", date);
    cmd.Parameters.AddWithValue("$sys", s.GetInt32());
    cmd.Parameters.AddWithValue("$dia", d.GetInt32());
    cmd.Parameters.AddWithValue("$pulse", (object?)pulse ?? DBNull.Value);
    cmd.Parameters.AddWithValue("$name", n.GetString());
    var id = (long)cmd.ExecuteScalar()!;
    return Results.Json(new { id, date, systolic = s.GetInt32(), diastolic = d.GetInt32(), pulse, name = n.GetString() });
});

app.MapDelete("/api/records/{id}", (long id) =>
{
    using var conn = new SqliteConnection($"Data Source={dbPath}");
    conn.Open();
    using var cmd = conn.CreateCommand();
    cmd.CommandText = "DELETE FROM records WHERE id = $id";
    cmd.Parameters.AddWithValue("$id", id);
    var changes = cmd.ExecuteNonQuery();
    if (changes > 0) return Results.Json(new { message = "删除成功" });
    return Results.NotFound(new { error = "记录不存在" });
});

if (Directory.Exists(staticPath))
{
    var provider = new PhysicalFileProvider(staticPath);
    app.UseDefaultFiles(new DefaultFilesOptions { FileProvider = provider });
    app.UseStaticFiles(new StaticFileOptions { FileProvider = provider });
    var indexInfo = provider.GetFileInfo("index.html");
    app.MapFallback(() => indexInfo.Exists
        ? Results.Stream(indexInfo.CreateReadStream(), "text/html")
        : Results.NotFound());
}

app.MapGet("/api/users", () => Results.Json(users));
app.Urls.Add($"http://{address}:{port}");
app.Run();