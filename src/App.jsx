import React, { useState, useEffect } from "react";
import {
  Activity,
  List,
  BarChart2,
  Plus,
  Heart,
  Clock,
  TrendingUp,
  TrendingDown,
} from "lucide-react";
import {
  DatePicker,
  Button,
  TabBar,
  Picker,
  Card,
  Space,
  Tag,
  Toast,
  Dialog,
} from "antd-mobile";
import "antd-mobile/es/global";
import { Line } from "react-chartjs-2";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from "chart.js";
import { format } from "date-fns";

// Register ChartJS components
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

// --- Components ---

const Navigation = ({ activeTab, setActiveTab }) => {
  return (
    <nav className="nav" style={{ padding: 0 }}>
      <TabBar activeKey={activeTab} onChange={(key) => setActiveTab(key)}>
        <TabBar.Item key="input" icon={<Plus />} title="记录" />
        <TabBar.Item key="list" icon={<List />} title="历史" />
        <TabBar.Item key="chart" icon={<BarChart2 />} title="趋势" />
      </TabBar>
    </nav>
  );
};

const FilterPanel = ({
  users,
  filter,
  setFilter,
  onApply,
  onReset,
  expanded,
  setExpanded,
}) => {
  const [nameVisible, setNameVisible] = useState(false);
  const [startVisible, setStartVisible] = useState(false);
  const [endVisible, setEndVisible] = useState(false);
  useEffect(() => {
    const params = {};
    const defaultName = users && users.length ? users[0] : "";
    if (filter.name || defaultName) params.name = filter.name || defaultName;
    if (filter.start)
      params.start = new Date(filter.start + "T00:00:00.000Z").toISOString();
    if (filter.end)
      params.end = new Date(filter.end + "T23:59:59.999Z").toISOString();
    onApply(params, false);
  }, [filter.name, filter.start, filter.end]);

  const reset = () => {
    onReset && onReset();
  };

  return (
    <div className="card">
      <Button
        onClick={() => setExpanded((v) => !v)}
        style={{ marginBottom: "0.5rem" }}
      >
        {expanded ? "收起筛选" : "展开筛选"}
      </Button>
      {expanded && (
        <>
          <div className="form-grid">
            <div className="input-group">
              <label className="label">姓名筛选</label>
              <Picker
                columns={[users.map((u) => ({ label: u, value: u }))]}
                value={filter.name ? [filter.name] : undefined}
                visible={nameVisible}
                onClose={() => setNameVisible(false)}
                onConfirm={(val) => {
                  setFilter((f) => ({ ...f, name: val[0] || "" }));
                  setNameVisible(false);
                }}
              >
                {() => (
                  <Button block onClick={() => setNameVisible(true)}>
                    {filter.name || (users && users.length ? users[0] : "全部")}
                  </Button>
                )}
              </Picker>
            </div>
            <div className="input-group">
              <label className="label">开始日期</label>
              <DatePicker
                precision="day"
                value={filter.start ? new Date(filter.start) : undefined}
                visible={startVisible}
                onClose={() => setStartVisible(false)}
                onConfirm={(val) => {
                  setFilter((f) => ({
                    ...f,
                    start: format(val, "yyyy-MM-dd"),
                  }));
                  setStartVisible(false);
                }}
              >
                {() => (
                  <Button block onClick={() => setStartVisible(true)}>
                    {filter.start || "请选择"}
                  </Button>
                )}
              </DatePicker>
            </div>
            <div className="input-group">
              <label className="label">结束日期</label>
              <DatePicker
                precision="day"
                value={filter.end ? new Date(filter.end) : undefined}
                visible={endVisible}
                onClose={() => setEndVisible(false)}
                onConfirm={(val) => {
                  setFilter((f) => ({ ...f, end: format(val, "yyyy-MM-dd") }));
                  setEndVisible(false);
                }}
              >
                {() => (
                  <Button block onClick={() => setEndVisible(true)}>
                    {filter.end || "请选择"}
                  </Button>
                )}
              </DatePicker>
            </div>
          </div>
          <Space style={{ marginTop: "0.5rem" }}>
            <Button
              size="small"
              onClick={() =>
                setFilter((f) => ({
                  ...f,
                  start: format(
                    new Date(Date.now() - 7 * 24 * 60 * 60 * 1000),
                    "yyyy-MM-dd"
                  ),
                  end: format(new Date(), "yyyy-MM-dd"),
                }))
              }
            >
              近7天
            </Button>
            <Button
              size="small"
              onClick={() =>
                setFilter((f) => ({
                  ...f,
                  start: format(
                    new Date(Date.now() - 30 * 24 * 60 * 60 * 1000),
                    "yyyy-MM-dd"
                  ),
                  end: format(new Date(), "yyyy-MM-dd"),
                }))
              }
            >
              近30天
            </Button>
            <Button
              size="small"
              onClick={() =>
                setFilter((f) => ({
                  ...f,
                  start: format(
                    new Date(Date.now() - 90 * 24 * 60 * 60 * 1000),
                    "yyyy-MM-dd"
                  ),
                  end: format(new Date(), "yyyy-MM-dd"),
                }))
              }
            >
              近90天
            </Button>
          </Space>
          <Button onClick={reset} style={{ marginTop: "0.5rem" }}>
            重置筛选
          </Button>
        </>
      )}
    </div>
  );
};

const InputView = ({ onSave, users }) => {
  const [systolic, setSystolic] = useState("");
  const [diastolic, setDiastolic] = useState("");
  const [pulse, setPulse] = useState("");
  const [name, setName] = useState("");
  const [namePickerVisible, setNamePickerVisible] = useState(false);
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [dateVisible, setDateVisible] = useState(false);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!systolic || !diastolic) return;

    const selectedName = name || (users && users.length ? users[0] : "");
    if (!selectedName) return;
    onSave({
      id: Date.now(),
      date: selectedDate.toISOString(),
      systolic: parseInt(systolic),
      diastolic: parseInt(diastolic),
      pulse: pulse ? parseInt(pulse) : null,
      name: selectedName,
    });

    setSystolic("");
    setDiastolic("");
    setPulse("");
    setName(users[0] || "");
  };

  return (
    <div className="container">
      <h1 className="title">记录血压</h1>
      <form onSubmit={handleSubmit}>
        <div className="card compact">
          <div className="form-grid">
            <div className="input-group">
              <label className="label">姓名</label>
              <Picker
                columns={[users.map((u) => ({ label: u, value: u }))]}
                value={[name || (users && users.length ? users[0] : "")]}
                visible={namePickerVisible}
                onClose={() => setNamePickerVisible(false)}
                onConfirm={(val) => {
                  setName(val[0] || "");
                  setNamePickerVisible(false);
                }}
              >
                {() => (
                  <Button block onClick={() => setNamePickerVisible(true)}>
                    {name || (users && users.length ? users[0] : "请选择")}
                  </Button>
                )}
              </Picker>
            </div>
            <div className="input-group">
              <label className="label">记录时间</label>
              <DatePicker
                precision="minute"
                value={selectedDate}
                visible={dateVisible}
                onClose={() => setDateVisible(false)}
                onConfirm={(val) => {
                  setSelectedDate(val);
                  setDateVisible(false);
                }}
              >
                {() => (
                  <Button block onClick={() => setDateVisible(true)}>
                    {format(selectedDate, "yyyy-MM-dd HH:mm")}
                  </Button>
                )}
              </DatePicker>
            </div>
            <div className="input-group">
              <label className="label">收缩压 (高压) mmHg</label>
              <input
                type="number"
                className="input"
                placeholder="120"
                value={systolic}
                onChange={(e) => setSystolic(e.target.value)}
                required
              />
            </div>
            <div className="input-group">
              <label className="label">舒张压 (低压) mmHg</label>
              <input
                type="number"
                className="input"
                placeholder="80"
                value={diastolic}
                onChange={(e) => setDiastolic(e.target.value)}
                required
              />
            </div>
            <div className="input-group">
              <label className="label">心率 (次/分) - 选填</label>
              <input
                type="number"
                className="input"
                placeholder="75"
                value={pulse}
                onChange={(e) => setPulse(e.target.value)}
              />
            </div>
          </div>
        </div>
        <Button type="submit" color="primary" block>
          保存记录
        </Button>
      </form>
    </div>
  );
};

const ListView = ({
  records,
  users,
  onFilter,
  filter,
  setFilter,
  expanded,
  setExpanded,
  meta,
  onDelete,
}) => {
  const [deleteId, setDeleteId] = useState(null);

  const getStatus = (sys, dia) => {
    if (sys >= 140 || dia >= 90)
      return { label: "高血压", color: "danger", tone: "var(--danger)" };
    if (sys >= 130 || dia >= 90)
      return { label: "偏高", color: "warning", tone: "#f59e0b" };
    return { label: "正常", color: "success", tone: "var(--text-main)" };
  };

  const sortedRecords = [...records].sort(
    (a, b) => new Date(b.date) - new Date(a.date)
  );

  const average = React.useMemo(() => {
    if (!sortedRecords.length) return { systolic: 0, diastolic: 0 };
    const sum = sortedRecords.reduce(
      (acc, r) => ({
        systolic: acc.systolic + r.systolic,
        diastolic: acc.diastolic + r.diastolic,
      }),
      { systolic: 0, diastolic: 0 }
    );
    return {
      systolic: sum.systolic / sortedRecords.length,
      diastolic: sum.diastolic / sortedRecords.length,
    };
  }, [sortedRecords]);

  const getPrevRecord = (idx) => {
    const cur = sortedRecords[idx];
    for (let j = idx + 1; j < sortedRecords.length; j++) {
      if ((sortedRecords[j].name || "") === (cur.name || ""))
        return sortedRecords[j];
    }
    return null;
  };

  return (
    <div className="container">
      <h1 className="title">历史记录</h1>
      <FilterPanel
        users={users}
        filter={filter}
        setFilter={setFilter}
        onApply={(p) => onFilter(p)}
        onReset={() => {
          setFilter({
            name: "",
            start: format(
              new Date(Date.now() - 30 * 24 * 60 * 60 * 1000),
              "yyyy-MM-dd"
            ),
            end: format(new Date(), "yyyy-MM-dd"),
          });
          onFilter({});
        }}
        expanded={expanded}
        setExpanded={setExpanded}
      />
      {sortedRecords.length === 0 ? (
        <div className="empty-state">
          <Activity size={48} style={{ opacity: 0.2, marginBottom: "1rem" }} />
          <p>暂无记录，快去添加第一条吧</p>
        </div>
      ) : (
        <>
          <Card style={{ marginBottom: "0.75rem" }}>
            <div className="summary-row">
              <div className="date">
                {filter.start || "—"} 至 {filter.end || "—"}
              </div>
              <div className="metric-row">
                <Tag color="primary" fill="light">
                  高压均值 {Math.round(average.systolic || 0)}
                </Tag>
                <Tag color="primary" fill="light">
                  低压均值 {Math.round(average.diastolic || 0)}
                </Tag>
              </div>
            </div>
          </Card>
          {sortedRecords.map((record, i) => {
            const { label, color, tone } = getStatus(
              record.systolic,
              record.diastolic
            );
            const initial = (record.name || "").slice(0, 1);
            const prev = getPrevRecord(i);
            const avgSys = Math.round(average.systolic || 0);
            const avgDia = Math.round(average.diastolic || 0);
            const dSysPrev = prev ? record.systolic - prev.systolic : null;
            const dDiaPrev = prev ? record.diastolic - prev.diastolic : null;
            const dSysAvg = record.systolic - avgSys;
            const dDiaAvg = record.diastolic - avgDia;
            return (
              <Card
                key={record.id}
                style={{ marginBottom: "0.75rem" }}
                title={
                  <div className="record-header">
                    <div className="avatar" aria-hidden>
                      {initial || "?"}
                    </div>
                    <div>
                      <div className="name">{record.name || "未知"}</div>
                      <div className="date">
                        <Clock size={14} style={{ opacity: 0.6 }} />
                        {format(new Date(record.date), "yyyy-MM-dd HH:mm")}
                      </div>
                    </div>
                  </div>
                }
                extra={
                  <Tag color={color} fill="light">
                    {label}
                  </Tag>
                }
              >
                <div className="bp-number" style={{ color: tone }}>
                  <span>{record.systolic}</span>
                  <span className="bp-separator">/</span>
                  <span>{record.diastolic}</span>
                  <span className="bp-unit">mmHg</span>
                </div>
                {record.pulse ? (
                  <div className="metric-row">
                    <Heart size={14} style={{ color: "var(--danger)" }} />
                    <Tag color="primary" fill="outline">
                      心率 {record.pulse}
                    </Tag>
                  </div>
                ) : null}
                {prev ? (
                  <div className="compare-row">
                    <span className="compare-label">较上次对比</span>
                    <Tag
                      color={
                        dSysPrev > 0
                          ? "warning"
                          : dSysPrev < 0
                          ? "success"
                          : "default"
                      }
                      fill="outline"
                    >
                      {dSysPrev > 0 ? (
                        <TrendingUp size={14} />
                      ) : dSysPrev < 0 ? (
                        <TrendingDown size={14} />
                      ) : null}
                      高压{" "}
                      {dSysPrev === 0
                        ? "持平"
                        : `${dSysPrev > 0 ? "+" : ""}${dSysPrev}`}
                    </Tag>
                    <Tag
                      color={
                        dDiaPrev > 0
                          ? "warning"
                          : dDiaPrev < 0
                          ? "success"
                          : "default"
                      }
                      fill="outline"
                    >
                      {dDiaPrev > 0 ? (
                        <TrendingUp size={14} />
                      ) : dDiaPrev < 0 ? (
                        <TrendingDown size={14} />
                      ) : null}
                      低压{" "}
                      {dDiaPrev === 0
                        ? "持平"
                        : `${dDiaPrev > 0 ? "+" : ""}${dDiaPrev}`}
                    </Tag>
                  </div>
                ) : null}
                <div className="compare-row">
                  <span className="compare-label">较均值对比</span>
                  <Tag
                    color={
                      dSysAvg > 0
                        ? "warning"
                        : dSysAvg < 0
                        ? "success"
                        : "default"
                    }
                    fill="outline"
                  >
                    {dSysAvg > 0 ? (
                      <TrendingUp size={14} />
                    ) : dSysAvg < 0 ? (
                      <TrendingDown size={14} />
                    ) : null}
                    高压{" "}
                    {dSysAvg === 0
                      ? "持平"
                      : `${dSysAvg > 0 ? "+" : ""}${dSysAvg}`}
                  </Tag>
                  <Tag
                    color={
                      dDiaAvg > 0
                        ? "warning"
                        : dDiaAvg < 0
                        ? "success"
                        : "default"
                    }
                    fill="outline"
                  >
                    {dDiaAvg > 0 ? (
                      <TrendingUp size={14} />
                    ) : dDiaAvg < 0 ? (
                      <TrendingDown size={14} />
                    ) : null}
                    低压{" "}
                    {dDiaAvg === 0
                      ? "持平"
                      : `${dDiaAvg > 0 ? "+" : ""}${dDiaAvg}`}
                  </Tag>
                </div>
                <div className="card-actions">
                  <Button
                    size="small"
                    color="danger"
                    block
                    onClick={(e) => {
                      e.stopPropagation();
                      setDeleteId(record.id);
                    }}
                  >
                    删除
                  </Button>
                </div>
              </Card>
            );
          })}
        </>
      )}
      <Card style={{ textAlign: "center", color: "var(--text-muted)" }}>
        <div>
          第 {meta.page} / {meta.totalPages} 页，共 {meta.total} 条
        </div>
        {!sortedRecords.length &&
        meta.total === 0 ? null : !sortedRecords.length ? null : meta.page >=
          meta.totalPages ? (
          <div>已加载全部</div>
        ) : (
          <div>下拉加载更多...</div>
        )}
      </Card>

      <Dialog
        visible={!!deleteId}
        content="确认删除该记录？"
        onClose={() => setDeleteId(null)}
        actions={[
          {
            key: "cancel",
            text: "取消",
            onClick: () => setDeleteId(null),
          },
          {
            key: "confirm",
            text: "确认",
            danger: true,
            bold: true,
            onClick: () => {
              onDelete?.(deleteId);
              setDeleteId(null);
            },
          },
        ]}
      />
    </div>
  );
};

const ChartView = ({
  records,
  users,
  onFilter,
  filter,
  setFilter,
  expanded,
  setExpanded,
}) => {
  const sortedRecords = [...records].sort(
    (a, b) => new Date(a.date) - new Date(b.date)
  );

  const data = {
    labels: sortedRecords.map((r) => format(new Date(r.date), "MM-dd")),
    datasets: [
      {
        label: "收缩压",
        data: sortedRecords.map((r) => r.systolic),
        borderColor: "#ef4444",
        backgroundColor: "rgba(239, 68, 68, 0.1)",
        tension: 0.4,
        fill: true,
      },
      {
        label: "舒张压",
        data: sortedRecords.map((r) => r.diastolic),
        borderColor: "#0ea5e9",
        backgroundColor: "rgba(14, 165, 233, 0.1)",
        tension: 0.4,
        fill: true,
      },
    ],
  };

  const options = {
    responsive: true,
    plugins: {
      legend: {
        position: "top",
        labels: { color: "#64748b" },
      },
      title: {
        display: false,
      },
    },
    scales: {
      y: {
        grid: { color: "rgba(0,0,0,0.05)" },
        ticks: { color: "#64748b" },
      },
      x: {
        grid: { display: false },
        ticks: { color: "#64748b" },
      },
    },
  };

  return (
    <div className="container">
      <h1 className="title">趋势分析</h1>
      <FilterPanel
        users={users}
        filter={filter}
        setFilter={setFilter}
        onApply={(p) => onFilter(p)}
        onReset={() => {
          setFilter({
            name: "",
            start: format(
              new Date(Date.now() - 30 * 24 * 60 * 60 * 1000),
              "yyyy-MM-dd"
            ),
            end: format(new Date(), "yyyy-MM-dd"),
          });
          onFilter({});
        }}
        expanded={expanded}
        setExpanded={setExpanded}
      />
      {records.length < 2 ? (
        <div className="empty-state">
          <BarChart2 size={48} style={{ opacity: 0.2, marginBottom: "1rem" }} />
          <p>需要至少两条记录才能显示趋势</p>
        </div>
      ) : (
        <div className="card">
          <Line data={data} options={options} />
        </div>
      )}
    </div>
  );
};

// --- Authentication ---

const TOKEN_KEY = "health_app_token";
const CODE_KEY = "health_app_code";

const LoginView = ({ onSuccess }) => {
  const [code, setCode] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleLogin = async (e) => {
    e.preventDefault();
    if (!code.trim()) {
      setError("请输入授权码");
      return;
    }

    setLoading(true);
    setError("");

    try {
      const response = await fetch(`${API_BASE}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ code: code.trim() }),
      });

      if (response.ok) {
        const data = await response.json();
        localStorage.setItem(TOKEN_KEY, data.token);
        localStorage.setItem(CODE_KEY, code.trim());
        Toast.show({ content: "登录成功", duration: 1500 });
        onSuccess();
      } else {
        setError("授权码错误，请重试");
      }
    } catch (err) {
      console.error("Login error:", err);
      setError("登录失败，请检查网络连接");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        minHeight: "100vh",
        padding: "1rem",
      }}
    >
      <div className="card" style={{ maxWidth: "400px", width: "100%" }}>
        <div style={{ textAlign: "center", marginBottom: "2rem" }}>
          <Heart
            size={48}
            style={{ color: "var(--primary)", marginBottom: "1rem" }}
          />
          <h1 className="title">健康管理系统</h1>
          <p style={{ color: "var(--text-muted)", fontSize: "0.875rem" }}>
            请输入授权码以继续
          </p>
        </div>

        <form onSubmit={handleLogin}>
          <div className="input-group" style={{ marginBottom: "1rem" }}>
            <label className="label">授权码</label>
            <input
              type="password"
              className="input"
              placeholder="请输入授权码"
              value={code}
              onChange={(e) => setCode(e.target.value)}
              disabled={loading}
              autoFocus
            />
          </div>

          {error && (
            <div
              style={{
                color: "var(--error)",
                fontSize: "0.875rem",
                marginBottom: "1rem",
              }}
            >
              {error}
            </div>
          )}

          <Button
            block
            color="primary"
            type="submit"
            disabled={loading}
            loading={loading}
          >
            {loading ? "登录中..." : "登录"}
          </Button>
        </form>
      </div>
    </div>
  );
};

// --- Main App ---

const API_BASE = "/api";

// API helper with token management
const apiRequest = async (url, options = {}) => {
  const token = localStorage.getItem(TOKEN_KEY);
  const headers = {
    ...options.headers,
    ...(token && { Authorization: `Bearer ${token}` }),
  };

  let response = await fetch(url, { ...options, headers });

  // Handle 401 - try to refresh token
  if (response.status === 401) {
    const savedCode = localStorage.getItem(CODE_KEY);
    if (savedCode) {
      try {
        const loginResponse = await fetch(`${API_BASE}/auth/login`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ code: savedCode }),
        });

        if (loginResponse.ok) {
          const data = await loginResponse.json();
          localStorage.setItem(TOKEN_KEY, data.token);

          // Retry original request with new token
          headers.Authorization = `Bearer ${data.token}`;
          response = await fetch(url, { ...options, headers });
        } else {
          // Refresh failed, clear storage
          localStorage.removeItem(TOKEN_KEY);
          localStorage.removeItem(CODE_KEY);
          window.location.reload(); // Force re-login
        }
      } catch (err) {
        console.error("Token refresh failed:", err);
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(CODE_KEY);
        window.location.reload();
      }
    } else {
      // No saved code, need to login
      localStorage.removeItem(TOKEN_KEY);
      window.location.reload();
    }
  }

  return response;
};

function App() {
  const [authenticated, setAuthenticated] = useState(false);
  const [activeTab, setActiveTab] = useState("input");
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [users, setUsers] = useState([]);
  const [filter, setFilter] = useState({ name: "", start: "", end: "" });
  const [expanded, setExpanded] = useState(false);
  const [page, setPage] = useState(1);
  const [hasMore, setHasMore] = useState(true);
  const PAGE_SIZE = 20;
  const [loadingMore, setLoadingMore] = useState(false);
  const [meta, setMeta] = useState({
    total: 0,
    page: 1,
    pageSize: PAGE_SIZE,
    totalPages: 0,
  });

  // 从后端加载数据
  useEffect(() => {
    // Check if user is authenticated
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token) {
      setLoading(false);
      return;
    }

    setAuthenticated(true);
    fetchUsers();
    const defaultStart = format(
      new Date(Date.now() - 30 * 24 * 60 * 60 * 1000),
      "yyyy-MM-dd"
    );
    const defaultEnd = format(new Date(), "yyyy-MM-dd");
    setFilter((f) => ({ ...f, start: defaultStart, end: defaultEnd }));
    fetchRecords();
  }, [authenticated]);

  const fetchRecords = async (params = {}, append = false) => {
    try {
      const p = append ? page + 1 : 1;
      const searchParams = new URLSearchParams({
        page: p,
        pageSize: PAGE_SIZE,
      });
      Object.entries(params).forEach(([k, v]) => {
        if (v) searchParams.set(k, v);
      });
      const qs = searchParams.toString();
      const response = await apiRequest(`${API_BASE}/records?${qs}`);
      const result = await response.json();
      const data = result.data || [];
      const meta = result.meta || {
        total: 0,
        page: p,
        pageSize: PAGE_SIZE,
        totalPages: 0,
      };
      if (append) {
        setRecords((prev) => [...prev, ...data]);
        setPage(meta.page);
        setLoadingMore(false);
      } else {
        setRecords(data);
        setPage(meta.page);
      }
      setHasMore(meta.page < meta.totalPages);
      setMeta(meta);
    } catch (error) {
      console.error("加载数据失败:", error);
    } finally {
      setLoading(false);
    }
  };

  const buildParamsFromFilter = () => {
    const params = {};
    if (filter.name) params.name = filter.name;
    if (filter.start)
      params.start = new Date(filter.start + "T00:00:00.000Z").toISOString();
    if (filter.end)
      params.end = new Date(filter.end + "T23:59:59.999Z").toISOString();
    return params;
  };

  useEffect(() => {
    const onScroll = () => {
      const nearBottom =
        window.innerHeight + window.scrollY >= document.body.offsetHeight - 50;
      if (nearBottom && hasMore && !loadingMore) {
        setLoadingMore(true);
        fetchRecords(buildParamsFromFilter(), true);
      }
    };
    window.addEventListener("scroll", onScroll);
    return () => window.removeEventListener("scroll", onScroll);
  }, [hasMore, loadingMore, filter, page]);

  const fetchUsers = async () => {
    try {
      const response = await apiRequest(`${API_BASE}/users`);
      const data = await response.json();
      setUsers(data);
    } catch (error) {
      console.error("加载用户失败:", error);
    }
  };

  const handleSave = async (newRecord) => {
    try {
      const response = await apiRequest(`${API_BASE}/records`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(newRecord),
      });
      const savedRecord = await response.json();
      setRecords((prev) => [...prev, savedRecord]);
      setActiveTab("list");
      Toast.show({ content: "保存成功", duration: 1500 });
    } catch (error) {
      console.error("保存失败:", error);
      Toast.show({
        content: "保存失败，请检查后端服务是否运行",
        duration: 2000,
      });
    }
  };

  const handleDeleteRecord = async (id) => {
    try {
      const resp = await apiRequest(`${API_BASE}/records/${id}`, {
        method: "DELETE",
      });
      if (resp.ok) {
        setRecords((prev) => prev.filter((r) => r.id !== id));
        Toast.show({ content: "删除成功", duration: 1500 });
      } else {
        const err = await resp.json().catch(() => ({}));
        Toast.show({ content: err.error || "删除失败", duration: 2000 });
      }
    } catch (e) {
      console.error(e);
      Toast.show({ content: "网络异常，删除失败", duration: 2000 });
    }
  };

  // Show login if not authenticated
  if (!authenticated) {
    return <LoginView onSuccess={() => setAuthenticated(true)} />;
  }

  if (loading) {
    return (
      <div
        style={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          height: "100vh",
        }}
      >
        <div style={{ textAlign: "center", color: "var(--text-muted)" }}>
          <Activity size={48} style={{ opacity: 0.3, marginBottom: "1rem" }} />
          <p>加载中...</p>
        </div>
      </div>
    );
  }

  return (
    <>
      <main style={{ minHeight: "100vh" }}>
        {activeTab === "input" && (
          <InputView onSave={handleSave} users={users} />
        )}
        {activeTab === "list" && (
          <ListView
            records={records}
            users={users}
            onFilter={(p) => fetchRecords(p)}
            filter={filter}
            setFilter={setFilter}
            expanded={expanded}
            setExpanded={setExpanded}
            meta={meta}
            onDelete={handleDeleteRecord}
          />
        )}
        {activeTab === "chart" && (
          <ChartView
            records={records}
            users={users}
            onFilter={(p) => fetchRecords(p)}
            filter={filter}
            setFilter={setFilter}
            expanded={expanded}
            setExpanded={setExpanded}
          />
        )}
      </main>
      <Navigation activeTab={activeTab} setActiveTab={setActiveTab} />
    </>
  );
}

export default App;
