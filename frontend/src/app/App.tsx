import { useState } from "react";
import {
  LayoutDashboard, FileText, Shield, Settings,
  Bell, Search, AlertTriangle,
  Plus, MoreHorizontal, Eye, RefreshCw,
  ArrowUpRight, Download, Clipboard,
  ChevronDown, ChevronRight,
  DollarSign, Lock, LogOut,
} from "lucide-react";
import {
  AreaChart, Area, BarChart, Bar, XAxis, YAxis,
  CartesianGrid, Tooltip, ResponsiveContainer,
} from "recharts";

// ─── Types ────────────────────────────────────────────────────
type Role = "admin" | "underwriter" | "claims_handler" | "agent" | "policyholder";
type View = "dashboard" | "policies" | "claims" | "underwriter" | "renewals" | "fraud" | "admin";

interface NavItem { view: View; label: string; icon: any; roles: Role[] }

// ─── Config ───────────────────────────────────────────────────
const ROLES: { value: Role; label: string; abbr: string }[] = [
  { value: "admin",          label: "System Administrator", abbr: "SYS" },
  { value: "underwriter",    label: "Underwriter",          abbr: "UW"  },
  { value: "claims_handler", label: "Claims Handler",       abbr: "CH"  },
  { value: "agent",          label: "Insurance Agent",      abbr: "AG"  },
  { value: "policyholder",   label: "Policyholder",         abbr: "PH"  },
];

const NAV: NavItem[] = [
  { view: "dashboard",   label: "Dashboard",               icon: LayoutDashboard, roles: ["admin","underwriter","claims_handler","agent","policyholder"] },
  { view: "policies",    label: "Policy Register",          icon: FileText,        roles: ["admin","underwriter","agent","policyholder"] },
  { view: "claims",      label: "Claims",                   icon: Clipboard,       roles: ["admin","claims_handler","agent","policyholder"] },
  { view: "underwriter", label: "Underwriter Workbench",    icon: Shield,          roles: ["admin","underwriter"] },
  { view: "renewals",    label: "Renewals",                 icon: RefreshCw,       roles: ["admin","underwriter","agent"] },
  { view: "fraud",       label: "Fraud Detection",          icon: AlertTriangle,   roles: ["admin","claims_handler"] },
  { view: "admin",       label: "Administration",           icon: Settings,        roles: ["admin"] },
];

// ─── Mock Data ────────────────────────────────────────────────
const premiumSeries = [
  { m: "Feb", collected: 2.10, target: 2.30 },
  { m: "Mar", collected: 2.45, target: 2.30 },
  { m: "Apr", collected: 2.21, target: 2.40 },
  { m: "May", collected: 2.68, target: 2.40 },
  { m: "Jun", collected: 2.84, target: 2.50 },
  { m: "Jul", collected: 2.39, target: 2.50 },
];

const claimsByType = [
  { t: "Property",  n: 32 },
  { t: "Liability", n: 18 },
  { t: "Auto",      n: 21 },
  { t: "Health",    n: 14 },
  { t: "Marine",    n: 11 },
  { t: "Cyber",     n:  9 },
];

interface Policy {
  id: string; holder: string; type: string;
  premium: number; status: string; expiry: string; risk: string;
}
const POLICIES: Policy[] = [
  { id: "POL-2024-001", holder: "John Meridian & Co.",      type: "Commercial Property",  premium: 12450,  status: "Active",       expiry: "2025-03-15", risk: "Medium"   },
  { id: "POL-2024-002", holder: "Thornfield Holdings Ltd",  type: "Marine Cargo",          premium: 34200,  status: "Active",       expiry: "2025-06-30", risk: "High"     },
  { id: "POL-2024-003", holder: "Blackwood Estate Trust",   type: "Life Insurance",        premium: 8900,   status: "Pending",      expiry: "2025-01-20", risk: "Low"      },
  { id: "POL-2024-004", holder: "Calloway Industries",      type: "General Liability",     premium: 22800,  status: "Active",       expiry: "2025-09-10", risk: "Medium"   },
  { id: "POL-2024-005", holder: "Harrington Group",         type: "Auto Fleet",            premium: 67400,  status: "Lapsed",       expiry: "2024-11-30", risk: "High"     },
  { id: "POL-2024-006", holder: "Elara Fontaine",           type: "Health Insurance",      premium: 4320,   status: "Active",       expiry: "2025-04-22", risk: "Low"      },
  { id: "POL-2024-007", holder: "Nova Dynamics Ltd",        type: "Cyber Liability",       premium: 18750,  status: "Under Review", expiry: "2025-07-14", risk: "Critical" },
  { id: "POL-2024-008", holder: "Belmont Financial Group",  type: "Directors & Officers",  premium: 45600,  status: "Active",       expiry: "2025-08-01", risk: "Medium"   },
];

interface Claim {
  id: string; policy: string; holder: string;
  type: string; date: string; reserve: number; status: string; adjuster: string;
}
const CLAIMS: Claim[] = [
  { id: "CLM-2024-0091", policy: "POL-2024-001", holder: "John Meridian & Co.",     type: "Fire Damage",           date: "2024-11-08", reserve: 145000,  status: "Under Investigation", adjuster: "R. Okafor"    },
  { id: "CLM-2024-0087", policy: "POL-2024-004", holder: "Calloway Industries",     type: "Third Party Liability", date: "2024-10-22", reserve: 28500,   status: "Approved",            adjuster: "S. Lindqvist" },
  { id: "CLM-2024-0082", policy: "POL-2024-006", holder: "Elara Fontaine",          type: "Medical Expenses",      date: "2024-10-14", reserve: 12300,   status: "Pending FNOL",        adjuster: "—"            },
  { id: "CLM-2024-0079", policy: "POL-2024-002", holder: "Thornfield Holdings Ltd", type: "Cargo Loss at Sea",     date: "2024-09-30", reserve: 89400,   status: "Fraud Alert",         adjuster: "K. Brennan"   },
  { id: "CLM-2024-0075", policy: "POL-2024-007", holder: "Nova Dynamics Ltd",       type: "Data Breach",           date: "2024-09-18", reserve: 210000,  status: "Awaiting Adjuster",   adjuster: "—"            },
  { id: "CLM-2024-0070", policy: "POL-2024-008", holder: "Belmont Financial Group", type: "D&O Liability",         date: "2024-08-27", reserve: 380000,  status: "Under Investigation", adjuster: "P. Hawthorne" },
];

const RENEWALS = [
  { id: "POL-2024-003", holder: "Blackwood Estate Trust",  type: "Life Insurance",        expiry: "2025-01-20", premium: 8900,   daysLeft: 38,  action: "Renewal Pending"          },
  { id: "POL-2024-001", holder: "John Meridian & Co.",     type: "Commercial Property",   expiry: "2025-03-15", premium: 12450,  daysLeft: 62,  action: "Awaiting Premium Review"  },
  { id: "POL-2024-006", holder: "Elara Fontaine",          type: "Health Insurance",      expiry: "2025-04-22", premium: 4320,   daysLeft: 104, action: "Auto-Renew Scheduled"     },
  { id: "POL-2024-002", holder: "Thornfield Holdings Ltd", type: "Marine Cargo",          expiry: "2025-06-30", premium: 34200,  daysLeft: 168, action: "Auto-Renew Scheduled"     },
];

const FRAUD_ALERTS = [
  { id: "FA-2024-007", claim: "CLM-2024-0079", holder: "Thornfield Holdings Ltd", score: 87, flags: ["Duplicate asset claim", "Prior claim pattern match"],  status: "Escalated",   analyst: "K. Brennan"   },
  { id: "FA-2024-005", claim: "CLM-2024-0075", holder: "Nova Dynamics Ltd",       score: 74, flags: ["Unusual claim timing", "First claim within 90 days"],  status: "Under Review", analyst: "—"            },
  { id: "FA-2024-003", claim: "CLM-2024-0082", holder: "Elara Fontaine",          score: 42, flags: ["Address mismatch on submission"],                       status: "Cleared",      analyst: "S. Lindqvist" },
];

const AUDIT_LOG = [
  { time: "09:41:22", user: "r.okafor@ipcms",    action: "Reserve updated",      target: "CLM-2024-0091",    detail: "$145,000 → $162,000"            },
  { time: "09:28:07", user: "admin@ipcms",        action: "User role modified",   target: "s.lindqvist@ipcms", detail: "Agent → Claims Handler"         },
  { time: "08:55:43", user: "m.chen@ipcms",       action: "Policy issued",        target: "POL-2024-008",     detail: "Belmont Financial — D&O"         },
  { time: "08:30:11", user: "k.brennan@ipcms",    action: "Fraud flag raised",    target: "CLM-2024-0079",    detail: "Duplicate asset indicator"      },
  { time: "07:59:55", user: "admin@ipcms",        action: "System table updated", target: "rate_matrix",      detail: "Marine risk index revised"      },
  { time: "07:42:18", user: "p.hawthorne@ipcms",  action: "Override submitted",   target: "POL-2024-008",     detail: "Limit +$200k — pending approval" },
];

const SYSTEM_USERS = [
  { id: 1, name: "Rachel Okafor",    email: "r.okafor@ipcms",   role: "Claims Handler",   office: "Lagos",     cert: "CH-2021-004", active: true  },
  { id: 2, name: "Stefan Lindqvist", email: "s.lindqvist@ipcms", role: "Claims Handler",  office: "Stockholm", cert: "CH-2020-018", active: true  },
  { id: 3, name: "Michael Chen",     email: "m.chen@ipcms",     role: "Underwriter",      office: "Singapore", cert: "UW-2019-007", active: true  },
  { id: 4, name: "Karen Brennan",    email: "k.brennan@ipcms",  role: "Claims Handler",   office: "Dublin",    cert: "CH-2022-011", active: true  },
  { id: 5, name: "Peter Hawthorne",  email: "p.hawthorne@ipcms", role: "Underwriter",     office: "London",    cert: "UW-2018-003", active: false },
  { id: 6, name: "Amara Diallo",     email: "a.diallo@ipcms",   role: "Insurance Agent",  office: "Dakar",     cert: "AG-2023-029", active: true  },
];

const SYS_CONFIG = [
  { label: "JWT Token Expiry — Standard",       value: "8 hours",                editable: true  },
  { label: "JWT Token Expiry — Underwriter",    value: "12 hours",               editable: true  },
  { label: "Renewal Notification Lead Time",    value: "45 days",                editable: true  },
  { label: "Claims Registration Window",        value: "14 calendar days",       editable: false },
  { label: "AML Check Provider",                value: "NationalSanctionsList v3.2", editable: false },
  { label: "KYC Verification Mode",             value: "Real-time · Active",     editable: false },
  { label: "Fraud Score Freeze Threshold",      value: "≥ 70 / 100",            editable: true  },
  { label: "Reinsurance Treaty Limit",          value: "$5,000,000.00",          editable: true  },
];

// ─── Formatters ───────────────────────────────────────────────
function fmt$(v: number) {
  return new Intl.NumberFormat("en-US", { style: "currency", currency: "USD", minimumFractionDigits: 2 }).format(v);
}

// ─── Atom components ──────────────────────────────────────────
const STATUS_STYLES: Record<string, string> = {
  "Active":                    "bg-emerald-50 text-emerald-700 border-emerald-200",
  "Pending":                   "bg-amber-50 text-amber-700 border-amber-200",
  "Pending FNOL":              "bg-amber-50 text-amber-700 border-amber-200",
  "Lapsed":                    "bg-slate-100 text-slate-500 border-slate-200",
  "Under Review":              "bg-sky-50 text-sky-700 border-sky-200",
  "Under Investigation":       "bg-sky-50 text-sky-700 border-sky-200",
  "Awaiting Adjuster":         "bg-violet-50 text-violet-700 border-violet-200",
  "Approved":                  "bg-emerald-50 text-emerald-700 border-emerald-200",
  "Fraud Alert":               "bg-red-50 text-red-700 border-red-200",
  "Escalated":                 "bg-red-50 text-red-700 border-red-200",
  "Cleared":                   "bg-emerald-50 text-emerald-700 border-emerald-200",
  "Auto-Renew Scheduled":      "bg-emerald-50 text-emerald-700 border-emerald-200",
  "Renewal Pending":           "bg-amber-50 text-amber-700 border-amber-200",
  "Awaiting Premium Review":   "bg-sky-50 text-sky-700 border-sky-200",
  "Inactive":                  "bg-slate-100 text-slate-500 border-slate-200",
};

function Badge({ status }: { status: string }) {
  const cls = STATUS_STYLES[status] ?? "bg-muted text-muted-foreground border-border";
  return (
    <span className={`inline-block text-[10px] font-mono px-1.5 py-0.5 rounded-sm border whitespace-nowrap ${cls}`}>
      {status}
    </span>
  );
}

function Risk({ level }: { level: string }) {
  const c: Record<string, string> = {
    Low:      "text-emerald-600",
    Medium:   "text-amber-600",
    High:     "text-orange-500",
    Critical: "text-red-600",
  };
  return <span className={`text-xs font-mono font-semibold ${c[level] ?? "text-muted-foreground"}`}>{level}</span>;
}

function SectionHeader({ title, sub }: { title: string; sub?: string }) {
  return (
    <div>
      <h2 className="font-['Barlow_Condensed'] text-xl font-semibold tracking-wide uppercase text-foreground">{title}</h2>
      {sub && <p className="text-[11px] text-muted-foreground font-mono mt-0.5">{sub}</p>}
    </div>
  );
}

function LoginView({ selectedRole, onSelectRole, onLogin }: {
  selectedRole: Role;
  onSelectRole: (role: Role) => void;
  onLogin: (role: Role) => void;
}) {
  const selectedLabel = ROLES.find(r => r.value === selectedRole)?.label ?? "System Administrator";

  return (
    <div className="min-h-screen bg-[radial-gradient(circle_at_top_left,_rgba(27,58,107,0.18),_transparent_35%),linear-gradient(135deg,_#f8fafc_0%,_#eef2ff_100%)] flex items-center justify-center p-4 sm:p-6">
      <div className="w-full max-w-5xl overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-2xl">
        <div className="grid lg:grid-cols-[1.08fr_0.92fr]">
          <div className="bg-[#1B3A6B] p-8 sm:p-10 text-white flex flex-col justify-between">
            <div>
              <div className="text-[11px] font-mono uppercase tracking-[0.3em] text-slate-300">IPCMS Secure Access</div>
              <h1 className="mt-3 text-3xl font-['Barlow_Condensed'] font-semibold tracking-wide">Insurance Policy Claims System</h1>
              <p className="mt-3 max-w-md text-sm text-slate-300">
                Sign in as the role you need to support policy, claims, underwriting, or customer operations.
              </p>
            </div>

            <div className="mt-8 rounded-xl border border-white/10 bg-white/10 p-4 backdrop-blur">
              <div className="text-[10px] font-mono uppercase tracking-[0.3em] text-slate-300">Switch Role</div>
              <div className="mt-3 space-y-2">
                {ROLES.map(role => {
                  const active = role.value === selectedRole;
                  return (
                    <button
                      key={role.value}
                      type="button"
                      onClick={() => onSelectRole(role.value)}
                      className={`w-full rounded-lg border px-3 py-2 text-left text-sm transition-colors ${active ? "border-[#E8411A] bg-[#E8411A]/15 text-white" : "border-white/10 bg-transparent text-slate-200 hover:bg-white/10"}`}
                    >
                      {role.label}
                    </button>
                  );
                })}
              </div>
            </div>
          </div>

          <div className="p-8 sm:p-10">
            <div className="text-[11px] font-mono uppercase tracking-[0.3em] text-[#E8411A]">Demo Login</div>
            <h2 className="mt-2 text-2xl font-semibold text-slate-900">Welcome back</h2>
            <p className="mt-2 text-sm text-slate-500">Choose the role and enter your credentials to continue.</p>

            <div className="mt-6 rounded-xl border border-slate-200 bg-slate-50 p-4">
              <div className="text-[10px] font-mono uppercase tracking-[0.3em] text-slate-500">Selected Role</div>
              <div className="mt-2 text-sm font-medium text-slate-800">{selectedLabel}</div>
            </div>

            <form
              className="mt-6 space-y-4"
              onSubmit={(event) => {
                event.preventDefault();
                onLogin(selectedRole);
              }}
            >
              <div>
                <label className="mb-2 block text-sm font-medium text-slate-700" htmlFor="email">Email Address</label>
                <input
                  id="email"
                  type="email"
                  defaultValue={`${selectedRole}@ipcms.local`}
                  className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm outline-none focus:border-[#1B3A6B] focus:ring-2 focus:ring-[#1B3A6B]/15"
                  placeholder="name@company.com"
                />
              </div>

              <div>
                <label className="mb-2 block text-sm font-medium text-slate-700" htmlFor="password">Password</label>
                <input
                  id="password"
                  type="password"
                  defaultValue="password123"
                  className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm outline-none focus:border-[#1B3A6B] focus:ring-2 focus:ring-[#1B3A6B]/15"
                  placeholder="Enter password"
                />
              </div>

              <button
                type="submit"
                className="w-full rounded-lg bg-[#E8411A] px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-[#c83517]"
              >
                Access Dashboard
              </button>
            </form>

            <p className="mt-4 text-xs text-slate-400">Demo mode: any valid email and password combination will sign you in.</p>
          </div>
        </div>
      </div>
    </div>
  );
}

function Th({ children }: { children: string }) {
  return (
    <th className="text-left px-4 py-2.5 text-[10px] font-mono text-muted-foreground uppercase tracking-wider font-medium whitespace-nowrap">
      {children}
    </th>
  );
}

function KpiCard({ label, value, sub, trend, icon: Icon, iconBg }: {
  label: string; value: string; sub: string;
  trend?: "up" | "alert";
  icon: any; iconBg: string;
}) {
  return (
    <div className="bg-card border border-border rounded-sm p-5 flex flex-col gap-3">
      <div className="flex items-start justify-between">
        <span className="text-[10px] font-mono text-muted-foreground uppercase tracking-widest leading-tight">{label}</span>
        <div className={`w-8 h-8 flex items-center justify-center rounded-sm ${iconBg}`}>
          <Icon size={14} />
        </div>
      </div>
      <div>
        <div className="text-2xl font-['Barlow_Condensed'] font-semibold tracking-tight">{value}</div>
        <div className="text-[11px] text-muted-foreground mt-0.5 flex items-center gap-1">
          {trend === "up"    && <ArrowUpRight size={10} className="text-emerald-500 shrink-0" />}
          {trend === "alert" && <AlertTriangle size={10} className="text-red-500 shrink-0" />}
          {sub}
        </div>
      </div>
    </div>
  );
}

// ─── Dashboard ────────────────────────────────────────────────
function DashboardView() {
  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
        <KpiCard label="Active Policies" value="1,247"  sub="+23 this month"       trend="up"    icon={FileText}      iconBg="bg-blue-50"    />
        <KpiCard label="Open Claims"     value="84"     sub="12 require attention"  trend="alert" icon={Clipboard}     iconBg="bg-amber-50"   />
        <KpiCard label="Premium MTD"     value="$2.84M" sub="+7.2% vs. target"      trend="up"    icon={DollarSign}    iconBg="bg-emerald-50" />
        <KpiCard label="Fraud Alerts"    value="7"      sub="2 escalated today"     trend="alert" icon={AlertTriangle} iconBg="bg-red-50"     />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-5 gap-4">
        {/* Premium trend */}
        <div className="lg:col-span-3 bg-card border border-border rounded-sm p-5">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-xs font-['Barlow_Condensed'] font-semibold tracking-widest uppercase">Premium Collections</h3>
              <p className="text-[10px] text-muted-foreground font-mono">Feb – Jul 2024 · USD millions</p>
            </div>
            <button className="text-[11px] text-muted-foreground border border-border px-2 py-1 rounded-sm hover:bg-muted transition-colors flex items-center gap-1">
              <Download size={10} /> Export
            </button>
          </div>
          <ResponsiveContainer width="100%" height={190}>
            <AreaChart data={premiumSeries} margin={{ top: 4, right: 4, left: -20, bottom: 0 }}>
              <defs>
                <linearGradient id="colGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%"  stopColor="#1B3A6B" stopOpacity={0.18} />
                  <stop offset="95%" stopColor="#1B3A6B" stopOpacity={0}    />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(0,0,0,0.05)" />
              <XAxis dataKey="m"  tick={{ fontSize: 10, fontFamily: "JetBrains Mono" }} axisLine={false} tickLine={false} />
              <YAxis             tick={{ fontSize: 10, fontFamily: "JetBrains Mono" }} axisLine={false} tickLine={false} tickFormatter={v => `$${v}M`} />
              <Tooltip
                formatter={(v: any) => [`$${v}M`, ""]}
                contentStyle={{ fontSize: 11, fontFamily: "JetBrains Mono", borderRadius: 2, border: "1px solid rgba(0,0,0,0.1)", padding: "6px 10px" }}
              />
              <Area type="monotone" dataKey="target"    stroke="rgba(0,0,0,0.18)" fill="none"          strokeDasharray="4 3" strokeWidth={1.5} dot={false} name="Target"    />
              <Area type="monotone" dataKey="collected" stroke="#1B3A6B"           fill="url(#colGrad)" strokeWidth={2}        dot={{ r: 3, fill: "#1B3A6B", strokeWidth: 0 }} name="Collected" />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        {/* Claims by type */}
        <div className="lg:col-span-2 bg-card border border-border rounded-sm p-5">
          <div className="mb-4">
            <h3 className="text-xs font-['Barlow_Condensed'] font-semibold tracking-widest uppercase">Claims by Type</h3>
            <p className="text-[10px] text-muted-foreground font-mono">Current quarter · count</p>
          </div>
          <ResponsiveContainer width="100%" height={190}>
            <BarChart data={claimsByType} layout="vertical" margin={{ top: 0, right: 8, left: -10, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(0,0,0,0.05)" horizontal={false} />
              <XAxis type="number"   tick={{ fontSize: 10, fontFamily: "JetBrains Mono" }} axisLine={false} tickLine={false} />
              <YAxis type="category" dataKey="t" tick={{ fontSize: 10, fontFamily: "JetBrains Mono" }} axisLine={false} tickLine={false} width={58} />
              <Tooltip contentStyle={{ fontSize: 11, fontFamily: "JetBrains Mono", borderRadius: 2, border: "1px solid rgba(0,0,0,0.1)", padding: "6px 10px" }} />
              <Bar dataKey="n" fill="#E8411A" radius={[0, 2, 2, 0]} name="Claims" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Recent claims table */}
      <div className="bg-card border border-border rounded-sm">
        <div className="flex items-center justify-between px-5 py-3 border-b border-border">
          <h3 className="text-xs font-['Barlow_Condensed'] font-semibold tracking-widest uppercase">Recent Claim Activity</h3>
          <span className="text-[10px] text-muted-foreground font-mono">Last 30 days</span>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border bg-muted/40">
                {["Claim ID","Policy","Type","Reserve","Adjuster","Status"].map(h => <Th key={h}>{h}</Th>)}
              </tr>
            </thead>
            <tbody>
              {CLAIMS.slice(0, 5).map((c, i) => (
                <tr key={c.id} className={`border-b border-border last:border-0 hover:bg-muted/30 transition-colors ${c.status === "Fraud Alert" ? "bg-red-50/30" : i % 2 !== 0 ? "bg-muted/10" : ""}`}>
                  <td className="px-4 py-3 font-mono text-xs text-primary font-medium">{c.id}</td>
                  <td className="px-4 py-3 font-mono text-xs text-muted-foreground">{c.policy}</td>
                  <td className="px-4 py-3 text-xs">{c.type}</td>
                  <td className="px-4 py-3 font-mono text-xs">{fmt$(c.reserve)}</td>
                  <td className="px-4 py-3 text-xs text-muted-foreground">{c.adjuster}</td>
                  <td className="px-4 py-3"><Badge status={c.status} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

// ─── Policy Register ──────────────────────────────────────────
function PoliciesView() {
  const [search, setSearch]       = useState("");
  const [statusFilter, setFilter] = useState("All");

  const filtered = POLICIES.filter(p => {
    const q = search.toLowerCase();
    const matchSearch = p.holder.toLowerCase().includes(q) || p.id.toLowerCase().includes(q);
    const matchStatus = statusFilter === "All" || p.status === statusFilter;
    return matchSearch && matchStatus;
  });

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <SectionHeader title="Policy Register" sub={`${POLICIES.length} policies · ${POLICIES.filter(p => p.status === "Active").length} active`} />
        <button className="flex items-center gap-1.5 bg-primary text-primary-foreground text-xs px-3 py-2 rounded-sm hover:bg-primary/90 transition-colors font-medium">
          <Plus size={12} /> New Policy
        </button>
      </div>

      <div className="flex flex-wrap items-center gap-3">
        <div className="relative">
          <Search size={12} className="absolute left-2.5 top-1/2 -translate-y-1/2 text-muted-foreground" />
          <input
            className="bg-card border border-border rounded-sm pl-8 pr-3 py-1.5 text-xs focus:outline-none focus:ring-1 focus:ring-primary/40 w-56"
            placeholder="Search holder or policy ID…"
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
        </div>
        <div className="flex items-center gap-1 flex-wrap">
          {["All","Active","Pending","Lapsed","Under Review"].map(s => (
            <button
              key={s}
              onClick={() => setFilter(s)}
              className={`text-[11px] px-2.5 py-1 rounded-sm border transition-colors font-mono ${
                statusFilter === s
                  ? "bg-primary text-primary-foreground border-primary"
                  : "bg-card border-border text-muted-foreground hover:bg-muted"
              }`}
            >{s}</button>
          ))}
        </div>
      </div>

      <div className="bg-card border border-border rounded-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border bg-muted/40">
                {["Policy ID","Policyholder","Product Type","Base Premium","Risk Level","Expiry Date","Status",""].map(h => <Th key={h}>{h}</Th>)}
              </tr>
            </thead>
            <tbody>
              {filtered.map((p, i) => (
                <tr key={p.id} className={`border-b border-border last:border-0 hover:bg-muted/30 transition-colors ${i % 2 !== 0 ? "bg-muted/10" : ""}`}>
                  <td className="px-4 py-3 font-mono text-xs text-primary font-medium">{p.id}</td>
                  <td className="px-4 py-3 text-sm font-medium">{p.holder}</td>
                  <td className="px-4 py-3 text-xs text-muted-foreground">{p.type}</td>
                  <td className="px-4 py-3 font-mono text-xs">{fmt$(p.premium)}</td>
                  <td className="px-4 py-3"><Risk level={p.risk} /></td>
                  <td className="px-4 py-3 font-mono text-xs text-muted-foreground">{p.expiry}</td>
                  <td className="px-4 py-3"><Badge status={p.status} /></td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2">
                      <button className="text-muted-foreground hover:text-primary transition-colors"><Eye size={13} /></button>
                      <button className="text-muted-foreground hover:text-primary transition-colors"><MoreHorizontal size={13} /></button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {filtered.length === 0 && (
          <div className="py-10 text-center text-xs text-muted-foreground font-mono">No policies match the current filter.</div>
        )}
      </div>
    </div>
  );
}

// ─── Claims ───────────────────────────────────────────────────
function ClaimsView() {
  const [tab, setTab] = useState<"list" | "fnol">("list");
  const [fnol, setFnol] = useState({ policy: "", date: "", type: "", description: "", location: "" });

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <SectionHeader
          title="Claims Management"
          sub={`${CLAIMS.length} open claims · ${CLAIMS.filter(c => c.status === "Fraud Alert").length} fraud flags`}
        />
        <div className="flex gap-1.5">
          {(["list","fnol"] as const).map(t => (
            <button
              key={t}
              onClick={() => setTab(t)}
              className={`text-[11px] px-3 py-1.5 border rounded-sm transition-colors font-mono ${
                tab === t ? "bg-primary text-primary-foreground border-primary" : "bg-card border-border text-muted-foreground hover:bg-muted"
              }`}
            >{t === "list" ? "All Claims" : "File FNOL"}</button>
          ))}
        </div>
      </div>

      {tab === "list" ? (
        <div className="bg-card border border-border rounded-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-border bg-muted/40">
                  {["Claim ID","Policy Ref.","Policyholder","Incident Type","Date","Gross Reserve","Adjuster","Status"].map(h => <Th key={h}>{h}</Th>)}
                </tr>
              </thead>
              <tbody>
                {CLAIMS.map((c, i) => (
                  <tr key={c.id} className={`border-b border-border last:border-0 hover:bg-muted/30 transition-colors ${c.status === "Fraud Alert" ? "bg-red-50/40" : i % 2 !== 0 ? "bg-muted/10" : ""}`}>
                    <td className="px-4 py-3 font-mono text-xs text-primary font-medium">{c.id}</td>
                    <td className="px-4 py-3 font-mono text-xs text-muted-foreground">{c.policy}</td>
                    <td className="px-4 py-3 text-xs font-medium">{c.holder}</td>
                    <td className="px-4 py-3 text-xs">{c.type}</td>
                    <td className="px-4 py-3 font-mono text-xs text-muted-foreground">{c.date}</td>
                    <td className="px-4 py-3 font-mono text-xs font-medium">{fmt$(c.reserve)}</td>
                    <td className="px-4 py-3 text-xs text-muted-foreground">{c.adjuster}</td>
                    <td className="px-4 py-3"><Badge status={c.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        <div className="bg-card border border-border rounded-sm p-6 max-w-2xl">
          <div className="mb-5 pb-4 border-b border-border">
            <h3 className="font-['Barlow_Condensed'] text-lg font-semibold tracking-wide uppercase">First Notice of Loss</h3>
            <p className="text-[11px] text-muted-foreground font-mono mt-0.5">
              Claims must be registered within 14 calendar days of the incident date. Fields marked * are required.
            </p>
          </div>
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-[10px] font-mono text-muted-foreground uppercase tracking-wider mb-1.5">Policy Number *</label>
                <input
                  className="w-full bg-input-background border border-border rounded-sm px-3 py-2 text-xs font-mono focus:outline-none focus:ring-1 focus:ring-primary/40"
                  placeholder="POL-YYYY-NNN"
                  value={fnol.policy}
                  onChange={e => setFnol(f => ({ ...f, policy: e.target.value }))}
                />
              </div>
              <div>
                <label className="block text-[10px] font-mono text-muted-foreground uppercase tracking-wider mb-1.5">Incident Date *</label>
                <input
                  type="date"
                  className="w-full bg-input-background border border-border rounded-sm px-3 py-2 text-xs font-mono focus:outline-none focus:ring-1 focus:ring-primary/40"
                  value={fnol.date}
                  onChange={e => setFnol(f => ({ ...f, date: e.target.value }))}
                />
              </div>
            </div>
            <div>
              <label className="block text-[10px] font-mono text-muted-foreground uppercase tracking-wider mb-1.5">Incident Type *</label>
              <select
                className="w-full bg-input-background border border-border rounded-sm px-3 py-2 text-xs focus:outline-none focus:ring-1 focus:ring-primary/40"
                value={fnol.type}
                onChange={e => setFnol(f => ({ ...f, type: e.target.value }))}
              >
                <option value="">— Select incident type —</option>
                {["Fire Damage","Flood Damage","Theft / Burglary","Third Party Liability","Medical Expenses","Cargo Loss","Data Breach","Auto Collision","Natural Disaster","Other"].map(t => (
                  <option key={t}>{t}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-[10px] font-mono text-muted-foreground uppercase tracking-wider mb-1.5">Incident Location *</label>
              <input
                className="w-full bg-input-background border border-border rounded-sm px-3 py-2 text-xs focus:outline-none focus:ring-1 focus:ring-primary/40"
                placeholder="Street address, city, country"
                value={fnol.location}
                onChange={e => setFnol(f => ({ ...f, location: e.target.value }))}
              />
            </div>
            <div>
              <label className="block text-[10px] font-mono text-muted-foreground uppercase tracking-wider mb-1.5">Loss Description *</label>
              <textarea
                className="w-full bg-input-background border border-border rounded-sm px-3 py-2 text-xs focus:outline-none focus:ring-1 focus:ring-primary/40 min-h-[88px] resize-y"
                placeholder="Provide a detailed description of the incident, nature of loss, and any witnesses…"
                value={fnol.description}
                onChange={e => setFnol(f => ({ ...f, description: e.target.value }))}
              />
            </div>
            <div className="pt-1 flex gap-2.5">
              <button className="bg-primary text-primary-foreground text-xs px-4 py-2 rounded-sm hover:bg-primary/90 transition-colors font-medium">
                Register FNOL
              </button>
              <button
                className="bg-muted text-muted-foreground text-xs px-4 py-2 rounded-sm hover:bg-muted/70 transition-colors"
                onClick={() => setFnol({ policy: "", date: "", type: "", description: "", location: "" })}
              >
                Clear Form
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

// ─── Underwriter Workbench ────────────────────────────────────
function UnderwriterView() {
  const [scores, setScores] = useState<Record<string,number>>({
    region: 65, assetAge: 40, priorClaims: 25, businessType: 55,
  });

  const composite = Math.round(Object.values(scores).reduce((a, b) => a + b, 0) / Object.keys(scores).length);
  const riskLabel = composite >= 70 ? "HIGH RISK" : composite >= 45 ? "MEDIUM RISK" : "LOW RISK";
  const riskColor = composite >= 70 ? "text-red-600" : composite >= 45 ? "text-amber-600" : "text-emerald-600";

  const SCORE_FIELDS = [
    { key: "region",       label: "Regional Risk Index"        },
    { key: "assetAge",     label: "Asset Age Factor"           },
    { key: "priorClaims",  label: "Prior Claims Frequency"     },
    { key: "businessType", label: "Business Classification"    },
  ];

  return (
    <div className="space-y-5">
      <SectionHeader title="Underwriter Workbench" sub="Risk evaluation · Premium rating · Override controls" />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        {/* Risk calculator */}
        <div className="lg:col-span-2 bg-card border border-border rounded-sm p-5">
          <h3 className="text-[10px] font-mono text-muted-foreground uppercase tracking-widest mb-5">Dynamic Risk Scoring Matrix</h3>
          <div className="space-y-5">
            {SCORE_FIELDS.map(({ key, label }) => (
              <div key={key}>
                <div className="flex justify-between items-center mb-1.5">
                  <span className="text-xs font-medium">{label}</span>
                  <span className="font-mono text-xs font-semibold w-6 text-right">{scores[key]}</span>
                </div>
                <input
                  type="range" min={0} max={100}
                  value={scores[key]}
                  onChange={e => setScores(s => ({ ...s, [key]: +e.target.value }))}
                  className="w-full h-1.5 cursor-pointer accent-primary"
                />
              </div>
            ))}
          </div>
          <div className="mt-6 pt-5 border-t border-border flex items-end justify-between">
            <div>
              <div className="text-[10px] font-mono text-muted-foreground uppercase tracking-widest">Composite Risk Score</div>
              <div className={`text-4xl font-['Barlow_Condensed'] font-bold mt-1 ${riskColor}`}>
                {composite} <span className="text-xl">— {riskLabel}</span>
              </div>
            </div>
            <button className="bg-primary text-primary-foreground text-xs px-4 py-2 rounded-sm hover:bg-primary/90 transition-colors font-medium">
              Generate Premium Quote
            </button>
          </div>
        </div>

        {/* Right column */}
        <div className="space-y-4">
          {/* Pending overrides */}
          <div className="bg-card border border-border rounded-sm p-5">
            <h3 className="text-[10px] font-mono text-muted-foreground uppercase tracking-widest mb-3">Pending Overrides</h3>
            <div className="space-y-3">
              {[
                { id: "OVR-084", policy: "POL-2024-008", type: "Limit Increase",  delta: "+$200,000",  by: "P. Hawthorne", status: "Pending"  },
                { id: "OVR-081", policy: "POL-2024-002", type: "Rate Override",   delta: "−8% premium", by: "M. Chen",      status: "Approved" },
              ].map(o => (
                <div key={o.id} className="border border-border rounded-sm p-3 space-y-1.5">
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-[10px] text-primary font-medium">{o.id}</span>
                    <Badge status={o.status} />
                  </div>
                  <div className="text-xs">{o.type} · <span className="font-mono font-semibold">{o.delta}</span></div>
                  <div className="text-[11px] text-muted-foreground font-mono">{o.policy} · {o.by}</div>
                  {o.status === "Pending" && (
                    <div className="flex gap-2 pt-0.5">
                      <button className="text-[11px] bg-emerald-600 text-white px-2.5 py-1 rounded-sm hover:bg-emerald-700 transition-colors">Approve</button>
                      <button className="text-[11px] bg-muted text-muted-foreground px-2.5 py-1 rounded-sm hover:bg-muted/60 transition-colors">Reject</button>
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* Actuarial templates */}
          <div className="bg-card border border-border rounded-sm p-5">
            <h3 className="text-[10px] font-mono text-muted-foreground uppercase tracking-widest mb-3">Actuarial Templates</h3>
            <div className="divide-y divide-border">
              {["Commercial Property v4.2","Marine Cargo v3.0","Cyber Risk v2.1","Auto Fleet v5.0","General Liability v3.3"].map(t => (
                <div key={t} className="flex items-center justify-between py-2 first:pt-0 last:pb-0">
                  <span className="text-xs">{t}</span>
                  <button className="text-[11px] text-primary hover:underline font-mono">Apply</button>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

// ─── Renewals ─────────────────────────────────────────────────
function RenewalsView() {
  return (
    <div className="space-y-4">
      <SectionHeader title="Renewal Pipeline" sub="Automatic notification dispatch triggers 45 days prior to policy expiry." />
      <div className="bg-card border border-border rounded-sm overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-border bg-muted/40">
              {["Policy ID","Policyholder","Product","Current Premium","Days to Expiry","Renewal Action",""].map(h => <Th key={h}>{h}</Th>)}
            </tr>
          </thead>
          <tbody>
            {RENEWALS.map((r, i) => (
              <tr key={r.id} className={`border-b border-border last:border-0 hover:bg-muted/30 transition-colors ${r.daysLeft <= 45 ? "bg-amber-50/30" : i % 2 !== 0 ? "bg-muted/10" : ""}`}>
                <td className="px-4 py-3 font-mono text-xs text-primary font-medium">{r.id}</td>
                <td className="px-4 py-3 text-xs font-medium">{r.holder}</td>
                <td className="px-4 py-3 text-xs text-muted-foreground">{r.type}</td>
                <td className="px-4 py-3 font-mono text-xs">{fmt$(r.premium)}</td>
                <td className="px-4 py-3">
                  <span className={`font-mono text-xs font-semibold ${r.daysLeft <= 45 ? "text-amber-600" : "text-muted-foreground"}`}>
                    {r.daysLeft <= 45 && "▲ "}{r.daysLeft}d
                  </span>
                </td>
                <td className="px-4 py-3"><Badge status={r.action} /></td>
                <td className="px-4 py-3">
                  <button className="text-[11px] text-primary hover:underline font-mono">Review</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Stats strip */}
      <div className="grid grid-cols-3 gap-3">
        {[
          { label: "Expiring ≤ 45 Days",  value: "1",    note: "1 requires manual review"  },
          { label: "Auto-Renew Scheduled", value: "2",    note: "Notifications sent"         },
          { label: "Avg. Premium Adj.",    value: "+3.4%", note: "Based on claims frequency" },
        ].map(s => (
          <div key={s.label} className="bg-card border border-border rounded-sm px-5 py-4">
            <div className="text-[10px] font-mono text-muted-foreground uppercase tracking-widest">{s.label}</div>
            <div className="text-2xl font-['Barlow_Condensed'] font-semibold mt-1">{s.value}</div>
            <div className="text-[11px] text-muted-foreground mt-0.5">{s.note}</div>
          </div>
        ))}
      </div>
    </div>
  );
}

// ─── Fraud Detection ──────────────────────────────────────────
function FraudView() {
  return (
    <div className="space-y-4">
      <SectionHeader
        title="Fraud Detection Engine"
        sub="Alerts with score ≥ 70 automatically freeze payment dispatch pending analyst clearance."
      />
      <div className="space-y-3">
        {FRAUD_ALERTS.map(a => (
          <div key={a.id} className={`bg-card border rounded-sm p-5 ${a.score >= 70 ? "border-red-200" : "border-border"}`}>
            <div className="flex items-start justify-between gap-4">
              <div className="space-y-2 flex-1 min-w-0">
                <div className="flex items-center gap-3 flex-wrap">
                  <span className="font-mono text-[10px] text-muted-foreground">{a.id}</span>
                  <span className="font-mono text-[10px] text-primary font-medium">{a.claim}</span>
                  <Badge status={a.status} />
                </div>
                <div className="text-sm font-semibold">{a.holder}</div>
                <div className="flex flex-wrap gap-1.5">
                  {a.flags.map(f => (
                    <span key={f} className="text-[10px] bg-red-50 text-red-700 border border-red-200 px-1.5 py-0.5 rounded-sm font-mono">{f}</span>
                  ))}
                </div>
                <div className="text-[11px] text-muted-foreground font-mono">Analyst: {a.analyst}</div>
              </div>
              <div className="text-right shrink-0">
                <div className={`text-4xl font-['Barlow_Condensed'] font-bold leading-none ${a.score >= 70 ? "text-red-600" : a.score >= 50 ? "text-amber-600" : "text-emerald-600"}`}>
                  {a.score}
                </div>
                <div className="text-[10px] font-mono text-muted-foreground mt-0.5">/ 100</div>
                {a.status !== "Cleared" && (
                  <div className="flex gap-1.5 mt-2.5 justify-end">
                    <button className="text-[11px] bg-primary text-primary-foreground px-2.5 py-1 rounded-sm hover:bg-primary/90 transition-colors">Investigate</button>
                    <button className="text-[11px] bg-emerald-600 text-white px-2.5 py-1 rounded-sm hover:bg-emerald-700 transition-colors">Clear</button>
                  </div>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

// ─── Administration ───────────────────────────────────────────
function AdminView() {
  const [tab, setTab] = useState<"users" | "audit" | "config">("users");

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <SectionHeader title="Administration" sub="User management · Audit trail · System configuration" />
        <div className="flex gap-1.5">
          {(["users","audit","config"] as const).map(t => (
            <button key={t} onClick={() => setTab(t)}
              className={`text-[11px] px-3 py-1.5 border rounded-sm transition-colors font-mono capitalize ${
                tab === t ? "bg-primary text-primary-foreground border-primary" : "bg-card border-border text-muted-foreground hover:bg-muted"
              }`}
            >{t === "audit" ? "Audit Log" : t === "config" ? "System Config" : "Users"}</button>
          ))}
        </div>
      </div>

      {tab === "users" && (
        <div className="bg-card border border-border rounded-sm overflow-hidden">
          <div className="flex items-center justify-between px-5 py-3 border-b border-border bg-muted/40">
            <span className="text-[10px] font-mono text-muted-foreground uppercase tracking-widest">{SYSTEM_USERS.length} registered users</span>
            <button className="flex items-center gap-1 text-xs bg-primary text-primary-foreground px-3 py-1.5 rounded-sm hover:bg-primary/90 transition-colors font-medium">
              <Plus size={11} /> Add User
            </button>
          </div>
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border bg-muted/20">
                {["Name","Email","Role","Office","Cert. No.","Status",""].map(h => <Th key={h}>{h}</Th>)}
              </tr>
            </thead>
            <tbody>
              {SYSTEM_USERS.map((u, i) => (
                <tr key={u.id} className={`border-b border-border last:border-0 hover:bg-muted/30 transition-colors ${i % 2 !== 0 ? "bg-muted/10" : ""}`}>
                  <td className="px-4 py-3 font-medium text-xs">{u.name}</td>
                  <td className="px-4 py-3 font-mono text-[11px] text-muted-foreground">{u.email}</td>
                  <td className="px-4 py-3 text-xs">{u.role}</td>
                  <td className="px-4 py-3 text-xs text-muted-foreground">{u.office}</td>
                  <td className="px-4 py-3 font-mono text-[11px] text-muted-foreground">{u.cert}</td>
                  <td className="px-4 py-3"><Badge status={u.active ? "Active" : "Inactive"} /></td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2.5">
                      <button className="text-[11px] text-primary hover:underline font-mono">Edit</button>
                      <button className="text-[11px] text-muted-foreground hover:text-red-600 transition-colors font-mono">
                        {u.active ? "Deactivate" : "Reactivate"}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {tab === "audit" && (
        <div className="bg-card border border-border rounded-sm overflow-hidden">
          <div className="px-5 py-3 border-b border-border bg-muted/40 flex items-center justify-between">
            <span className="text-[10px] font-mono text-muted-foreground uppercase tracking-widest">Immutable Audit Log · 2024-11-13</span>
            <button className="text-[11px] text-muted-foreground hover:text-primary flex items-center gap-1 transition-colors font-mono">
              <Download size={10} /> Export CSV
            </button>
          </div>
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border bg-muted/20">
                {["Time","User","Action","Target","Detail"].map(h => <Th key={h}>{h}</Th>)}
              </tr>
            </thead>
            <tbody>
              {AUDIT_LOG.map((e, i) => (
                <tr key={i} className={`border-b border-border last:border-0 hover:bg-muted/30 transition-colors ${i % 2 !== 0 ? "bg-muted/10" : ""}`}>
                  <td className="px-4 py-3 font-mono text-[11px] text-muted-foreground">{e.time}</td>
                  <td className="px-4 py-3 font-mono text-[11px] text-primary">{e.user}</td>
                  <td className="px-4 py-3 text-xs">{e.action}</td>
                  <td className="px-4 py-3 font-mono text-[11px] text-muted-foreground">{e.target}</td>
                  <td className="px-4 py-3 text-xs text-muted-foreground">{e.detail}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {tab === "config" && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-3">
          {SYS_CONFIG.map(cfg => (
            <div key={cfg.label} className="bg-card border border-border rounded-sm px-5 py-4 flex items-center justify-between gap-4">
              <div className="min-w-0">
                <div className="text-[10px] font-mono text-muted-foreground uppercase tracking-widest">{cfg.label}</div>
                <div className="text-sm font-medium mt-0.5 truncate">{cfg.value}</div>
              </div>
              {cfg.editable ? (
                <button className="text-[11px] text-primary border border-primary/25 px-2.5 py-1 rounded-sm hover:bg-primary/5 transition-colors font-mono shrink-0">
                  Edit
                </button>
              ) : (
                <Lock size={12} className="text-muted-foreground shrink-0" />
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

// ─── Sidebar ──────────────────────────────────────────────────
function Sidebar({ role, view, setView, onRoleSelect, onLogout }: {
  role: Role; view: View;
  setView: (v: View) => void;
  onRoleSelect: (r: Role) => void;
  onLogout: () => void;
}) {
  const [roleOpen, setRoleOpen] = useState(false);
  const currentRole = ROLES.find(r => r.value === role)!;
  const accessible  = NAV.filter(n => n.roles.includes(role));

  return (
    <aside className="w-52 shrink-0 flex flex-col h-full" style={{ background: "#1B3A6B" }}>
      {/* Logo */}
      <div className="px-4 py-4 border-b" style={{ borderColor: "rgba(255,255,255,0.1)" }}>
        <div className="flex items-center gap-2.5">
          <div className="w-7 h-7 rounded-sm flex items-center justify-center" style={{ background: "#E8411A" }}>
            <Shield size={13} className="text-white" />
          </div>
          <div>
            <div className="text-white text-xs font-['Barlow_Condensed'] font-bold tracking-[0.18em] uppercase leading-none">IPCMS</div>
            <div className="text-[9px] font-mono leading-none mt-0.5" style={{ color: "rgba(255,255,255,0.35)" }}>Enterprise · v1.0</div>
          </div>
        </div>
      </div>

      {/* Nav */}
      <nav className="flex-1 py-2 overflow-y-auto">
        <div className="px-4 py-2">
          <span className="text-[9px] font-mono uppercase tracking-[0.15em]" style={{ color: "rgba(255,255,255,0.3)" }}>Navigation</span>
        </div>
        {accessible.map(item => {
          const Icon = item.icon;
          const active = view === item.view;
          return (
            <button
              key={item.view}
              onClick={() => setView(item.view)}
              className={`w-full flex items-center gap-2.5 px-4 py-2.5 text-left transition-all ${
                active
                  ? "border-r-2 font-medium"
                  : "hover:opacity-80"
              }`}
              style={active
                ? { background: "rgba(255,255,255,0.12)", color: "#fff", borderColor: "#E8411A" }
                : { color: "rgba(255,255,255,0.5)" }
              }
            >
              <Icon size={13} />
              <span className="text-[11px] font-mono">{item.label}</span>
            </button>
          );
        })}
      </nav>

      {/* Role switcher */}
      <div className="border-t p-3" style={{ borderColor: "rgba(255,255,255,0.1)" }}>
        <button
          onClick={() => setRoleOpen(!roleOpen)}
          className="w-full flex items-center gap-2 px-2 py-1.5 rounded-sm transition-colors hover:bg-white/10"
        >
          <div className="w-7 h-7 rounded-full flex items-center justify-center text-white text-[10px] font-mono font-bold shrink-0" style={{ background: "#E8411A" }}>
            {currentRole.abbr}
          </div>
          <div className="flex-1 text-left min-w-0">
            <div className="text-white text-[11px] font-medium truncate leading-tight">Demo User</div>
            <div className="text-[9px] font-mono truncate leading-tight" style={{ color: "rgba(255,255,255,0.4)" }}>{currentRole.label}</div>
          </div>
          <ChevronDown size={11} className={`transition-transform shrink-0 ${roleOpen ? "rotate-180" : ""}`} style={{ color: "rgba(255,255,255,0.4)" }} />
        </button>

        {roleOpen && (
          <div className="mt-1 rounded-sm overflow-hidden border" style={{ background: "#0F2347", borderColor: "rgba(255,255,255,0.1)" }}>
            <div className="px-3 pt-2 pb-1">
              <span className="text-[9px] font-mono uppercase tracking-wider" style={{ color: "rgba(255,255,255,0.35)" }}>Switch Role</span>
            </div>
            {ROLES.map(r => (
              <button
                key={r.value}
                onClick={() => { onRoleSelect(r.value); setRoleOpen(false); setView("dashboard"); }}
                className="w-full px-3 py-2 text-left text-[11px] font-mono transition-colors flex items-center gap-2"
                style={role === r.value
                  ? { background: "rgba(255,255,255,0.12)", color: "#fff" }
                  : { color: "rgba(255,255,255,0.5)" }
                }
              >
                {role === r.value && <div className="w-1 h-1 rounded-full bg-[#E8411A]" />}
                {role !== r.value && <div className="w-1 h-1" />}
                {r.label}
              </button>
            ))}
          </div>
        )}

        <button
          onClick={onLogout}
          className="mt-3 w-full flex items-center justify-center gap-2 rounded-sm border border-white/10 px-2 py-2 text-[11px] font-mono text-white/80 transition-colors hover:bg-white/10 hover:text-white"
        >
          <LogOut size={12} />
          Log out
        </button>
      </div>
    </aside>
  );
}

// ─── Topbar ───────────────────────────────────────────────────
function TopBar({ view }: { view: View }) {
  const nav = NAV.find(n => n.view === view);
  const [notificationsOpen, setNotificationsOpen] = useState(false);

  const notifications = [
    { title: "Claim escalated", detail: "CLM-2024-0091 requires review", time: "2m ago" },
    { title: "Renewal reminder", detail: "POL-2024-003 is due for action", time: "18m ago" },
    { title: "Fraud alert", detail: "New suspicious pattern detected", time: "1h ago" },
  ];

  return (
    <header className="h-12 bg-card border-b border-border flex items-center justify-between px-5 shrink-0">
      <div className="flex items-center gap-1.5 text-[11px] font-mono text-muted-foreground">
        <span>IPCMS</span>
        <ChevronRight size={11} />
        <span className="text-foreground font-medium">{nav?.label ?? ""}</span>
      </div>
      <div className="flex items-center gap-3 relative">
        <div className="relative">
          <Search size={11} className="absolute left-2.5 top-1/2 -translate-y-1/2 text-muted-foreground" />
          <input
            className="bg-muted/50 border border-border rounded-sm pl-7 pr-3 py-1.5 text-[11px] focus:outline-none focus:ring-1 focus:ring-primary/30 w-40"
            placeholder="Quick search…"
          />
        </div>
        <div className="relative">
          <button
            onClick={() => setNotificationsOpen(!notificationsOpen)}
            className="relative p-1.5 text-muted-foreground hover:text-foreground transition-colors"
          >
            <Bell size={14} />
            <span className="absolute top-0.5 right-0.5 w-1.5 h-1.5 rounded-full" style={{ background: "#E8411A" }} />
          </button>

          {notificationsOpen && (
            <div className="absolute right-0 top-10 w-72 rounded-lg border border-border bg-white shadow-lg z-20">
              <div className="flex items-center justify-between border-b border-border px-3 py-2">
                <span className="text-[11px] font-semibold uppercase tracking-wider text-foreground">Notifications</span>
                <span className="text-[10px] font-mono text-muted-foreground">3 new</span>
              </div>
              <div className="max-h-64 overflow-y-auto">
                {notifications.map((item, index) => (
                  <div key={index} className="border-b border-border px-3 py-2.5 last:border-0">
                    <div className="flex items-start justify-between gap-2">
                      <div>
                        <div className="text-sm font-medium text-foreground">{item.title}</div>
                        <div className="text-xs text-muted-foreground">{item.detail}</div>
                      </div>
                      <span className="text-[10px] font-mono text-muted-foreground shrink-0">{item.time}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
        <div className="w-px h-4 bg-border" />
        <span className="text-[10px] font-mono text-muted-foreground">2024-11-13 · 09:41 UTC</span>
      </div>
    </header>
  );
}

// ─── App ──────────────────────────────────────────────────────
export default function App() {
  const [role, setRole] = useState<Role>("admin");
  const [view, setView] = useState<View>("dashboard");
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [selectedRole, setSelectedRole] = useState<Role>("admin");

  const handleLogin = (nextRole: Role) => {
    setRole(nextRole);
    setSelectedRole(nextRole);
    setView("dashboard");
    setIsAuthenticated(true);
  };

  const handleLogout = () => {
    setIsAuthenticated(false);
    setView("dashboard");
  };

  const handleRoleSelect = (nextRole: Role) => {
    setSelectedRole(nextRole);
    setIsAuthenticated(false);
    setView("dashboard");
  };

  if (!isAuthenticated) {
    return <LoginView selectedRole={selectedRole} onSelectRole={setSelectedRole} onLogin={handleLogin} />;
  }

  return (
    <div className="flex h-screen bg-background overflow-hidden" style={{ fontFamily: "'Inter', system-ui, sans-serif" }}>
      <Sidebar role={role} view={view} setView={setView} onRoleSelect={handleRoleSelect} onLogout={handleLogout} />
      <div className="flex-1 flex flex-col overflow-hidden min-w-0">
        <TopBar view={view} />
        <main className="flex-1 overflow-y-auto p-5">
          {view === "dashboard"   && <DashboardView />}
          {view === "policies"    && <PoliciesView />}
          {view === "claims"      && <ClaimsView />}
          {view === "underwriter" && <UnderwriterView />}
          {view === "renewals"    && <RenewalsView />}
          {view === "fraud"       && <FraudView />}
          {view === "admin"       && <AdminView />}
        </main>
      </div>
    </div>
  );
}
