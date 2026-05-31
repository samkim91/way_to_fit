
// Way To Fit — Admin Competition Screens
// Shared to window for use in main canvas

const { useState, useEffect, useRef } = React;

/* ─── Icon helper ──────────────────────────────────────────── */
function AdminIcon({ name, size = 16, color = 'currentColor', strokeWidth = 1.5 }) {
  const ref = useRef(null);
  useEffect(() => {
    if (ref.current && window.lucide) {
      ref.current.innerHTML = '';
      const iconData = window.lucide[name];
      const svg = iconData ? window.lucide.createElement(iconData) : null;
      if (svg) {
        svg.setAttribute('width', size);
        svg.setAttribute('height', size);
        svg.setAttribute('color', color);
        svg.setAttribute('stroke-width', strokeWidth);
        ref.current.appendChild(svg);
      }
    }
  }, [name, size, color, strokeWidth]);
  return <span ref={ref} style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }} />;
}

/* ─── Design tokens (dark-mode aware) ──────────────────────── */
function getTokens(dark) {
  return {
    bg:          dark ? '#0F172A' : '#F8F9FA',
    card:        dark ? '#1E293B' : '#FFFFFF',
    sidebar:     dark ? '#111827' : '#FFFFFF',
    border:      dark ? '#2D3748' : '#E2E8F0',
    muted:       dark ? '#1A2535' : '#F1F5F9',
    fg:          dark ? '#F1F5F9' : '#1A202C',
    fgMuted:     dark ? '#94A3B8' : '#718096',
    fgSubtle:    dark ? '#475569' : '#94A3B8',
    primary:     '#2563EB',
    primaryHover:'#1D4ED8',
    primarySubtle: dark ? '#1E3A8A' : '#EFF6FF',
    primaryMuted:  dark ? '#1E40AF' : '#DBEAFE',
    destructive: '#E53E3E',
    headerBg:    dark ? '#111827' : '#FFFFFF',
  };
}

/* ─── Status badge configs ──────────────────────────────────── */
const COMP_STATUS = {
  DRAFT:                { label: 'DRAFT',              bg: '#94A3B8', color: '#fff' },
  PUBLISHED:            { label: 'PUBLISHED',          bg: '#64748B', color: '#fff' },
  REGISTRATION_OPEN:    { label: 'REGISTRATION_OPEN',  bg: '#2563EB', color: '#fff' },
  REGISTRATION_CLOSED:  { label: 'REGISTRATION_CLOSED',bg: '#E53E3E', color: '#fff' },
  IN_PROGRESS:          { label: 'IN_PROGRESS',        bg: '#F97316', color: '#fff' },
  COMPLETED:            { label: 'COMPLETED',          bg: '#22C55E', color: '#fff' },
};

const SCORE_STATUS = {
  SUBMITTED:    { label: '제출됨',  bg: 'transparent', color: '#64748B', border: '#CBD5E1' },
  UNDER_REVIEW: { label: '검토 중', bg: 'transparent', color: '#2563EB', border: '#93C5FD' },
  APPROVED:     { label: '승인',    bg: '#22C55E',     color: '#fff',    border: 'transparent' },
  ADJUSTED:     { label: '조정됨',  bg: '#F97316',     color: '#fff',    border: 'transparent' },
  REJECTED:     { label: '거절됨',  bg: 'transparent', color: '#E53E3E', border: '#FCA5A5' },
};

const REG_STATUS = {
  PENDING:   { label: 'PENDING',   bg: '#FEF3C7', color: '#92400E' },
  CONFIRMED: { label: 'CONFIRMED', bg: '#C6F6D5', color: '#276749' },
  REJECTED:  { label: 'REJECTED',  bg: '#FED7D7', color: '#9B2C2C' },
};

/* ─── Badge ──────────────────────────────────────────────────── */
function Badge({ cfg, style: extraStyle }) {
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: 3,
      background: cfg.bg, color: cfg.color,
      border: cfg.border ? `1px solid ${cfg.border}` : 'none',
      padding: '2px 8px', borderRadius: 6,
      fontSize: 10, fontWeight: 600, letterSpacing: '0.04em',
      whiteSpace: 'nowrap', ...extraStyle,
    }}>{cfg.label}</span>
  );
}

/* ─── Sidebar ───────────────────────────────────────────────── */
const ADMIN_NAV = [
  { id: 'dashboard',    label: '대시보드',   icon: 'LayoutDashboard' },
  { id: 'members',      label: '회원 관리',  icon: 'Users' },
  { id: 'schedule',     label: '스케줄',     icon: 'Calendar' },
  { id: 'wod',          label: 'WOD 관리',   icon: 'Dumbbell' },
  { id: 'divider',      label: '',           icon: '' },
  { id: 'competitions', label: '대회 관리',  icon: 'Trophy' },
];

function AdminSidebar({ active, onNav, dark }) {
  const t = getTokens(dark);
  return (
    <aside style={{
      width: 220, flexShrink: 0,
      background: t.sidebar,
      borderRight: `1px solid ${t.border}`,
      display: 'flex', flexDirection: 'column',
      height: '100%',
    }}>
      <div style={{ padding: '16px 16px 12px', borderBottom: `1px solid ${t.border}` }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <div style={{ width: 28, height: 28, background: '#2563EB', borderRadius: 7, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <AdminIcon name="Dumbbell" size={14} color="#fff" />
          </div>
          <div>
            <div style={{ fontSize: 13, fontWeight: 700, color: t.fg, letterSpacing: '-0.01em' }}>Way To Fit</div>
            <div style={{ fontSize: 9, color: t.fgMuted, letterSpacing: '0.06em', textTransform: 'uppercase' }}>Admin</div>
          </div>
        </div>
      </div>
      <nav style={{ flex: 1, padding: '10px 8px', display: 'flex', flexDirection: 'column', gap: 1 }}>
        {ADMIN_NAV.map((item, i) => {
          if (item.id === 'divider') return <div key={i} style={{ height: 1, background: t.border, margin: '6px 4px' }} />;
          const isActive = item.id === active;
          return (
            <button key={item.id} onClick={() => onNav(item.id)} style={{
              display: 'flex', alignItems: 'center', gap: 8,
              padding: '8px 10px', borderRadius: 7, border: 'none', cursor: 'pointer',
              background: isActive ? (dark ? '#1E3A8A' : '#EFF6FF') : 'transparent',
              color: isActive ? '#2563EB' : t.fgMuted,
              fontFamily: 'inherit', fontSize: 12, fontWeight: isActive ? 600 : 400,
              textAlign: 'left', transition: 'background 150ms',
            }}>
              <AdminIcon name={item.icon} size={15} color={isActive ? '#2563EB' : t.fgMuted} />
              {item.label}
              {item.id === 'competitions' && (
                <span style={{ marginLeft: 'auto', background: dark ? '#1E3A8A' : '#DBEAFE', color: '#2563EB', fontSize: 9, fontWeight: 700, padding: '1px 5px', borderRadius: 9999 }}>NEW</span>
              )}
            </button>
          );
        })}
      </nav>
      <div style={{ padding: '10px 12px', borderTop: `1px solid ${t.border}`, display: 'flex', alignItems: 'center', gap: 8 }}>
        <div style={{ width: 28, height: 28, borderRadius: '50%', background: '#2563EB', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <span style={{ fontSize: 10, fontWeight: 700, color: '#fff' }}>박</span>
        </div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ fontSize: 11, fontWeight: 600, color: t.fg }}>박주최</div>
          <div style={{ fontSize: 9, color: t.fgMuted }}>organizer@waytofit.kr</div>
        </div>
        <AdminIcon name="LogOut" size={13} color={t.fgSubtle} />
      </div>
    </aside>
  );
}

/* ─── Header ────────────────────────────────────────────────── */
function AdminHeader({ title, breadcrumb, actions, dark }) {
  const t = getTokens(dark);
  return (
    <div style={{
      height: 56, background: t.headerBg, borderBottom: `1px solid ${t.border}`,
      display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      padding: '0 20px', flexShrink: 0,
    }}>
      <div>
        {breadcrumb && <div style={{ fontSize: 10, color: t.fgMuted, marginBottom: 1 }}>{breadcrumb}</div>}
        <div style={{ fontSize: 14, fontWeight: 700, color: t.fg }}>{title}</div>
      </div>
      <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
        {actions}
      </div>
    </div>
  );
}

/* ─── Btn ───────────────────────────────────────────────────── */
function Btn({ children, variant = 'primary', icon, small, onClick, dark }) {
  const t = getTokens(dark);
  const styles = {
    primary:  { bg: '#2563EB', color: '#fff', border: 'none' },
    secondary:{ bg: t.card,    color: t.fg,   border: `1px solid ${t.border}` },
    ghost:    { bg: 'transparent', color: t.fgMuted, border: `1px solid ${t.border}` },
    danger:   { bg: '#E53E3E', color: '#fff', border: 'none' },
  };
  const s = styles[variant] || styles.primary;
  return (
    <button onClick={onClick} style={{
      display: 'inline-flex', alignItems: 'center', gap: 5,
      padding: small ? '5px 10px' : '7px 14px',
      background: s.bg, color: s.color, border: s.border,
      borderRadius: 7, fontSize: small ? 11 : 12, fontWeight: 500,
      cursor: 'pointer', fontFamily: 'inherit',
    }}>
      {icon && <AdminIcon name={icon} size={small ? 12 : 13} color={s.color} />}
      {children}
    </button>
  );
}

/* ════════════════════════════════════════════════════════════════
   SCREEN 1 — 대회 목록
═══════════════════════════════════════════════════════════════ */
const COMPETITIONS = [
  {
    id: 1, name: '2026 Seoul CrossFit Open',
    dateRange: '2026.05.01 — 2026.05.10',
    regRange: '2026.04.01 — 2026.04.30',
    participants: 47,
    status: 'REGISTRATION_OPEN',
  },
  {
    id: 2, name: '2026 Summer Throwdown',
    dateRange: '2026.07.15 — 2026.07.16',
    regRange: '미설정',
    participants: 0,
    status: 'DRAFT',
  },
  {
    id: 3, name: '2025 Winter CrossFit Challenge',
    dateRange: '2025.12.20 — 2025.12.21',
    regRange: '2025.11.01 — 2025.11.30',
    participants: 134,
    status: 'COMPLETED',
  },
  {
    id: 4, name: '2026 Spring Box War',
    dateRange: '2026.03.22 — 2026.03.22',
    regRange: '2026.02.01 — 2026.03.10',
    participants: 88,
    status: 'IN_PROGRESS',
  },
];

function CompetitionListScreen({ dark, compStatus }) {
  const t = getTokens(dark);
  const [filter, setFilter] = useState('전체');
  const filters = ['전체', '신청중', '진행중', '종료'];
  const filtered = COMPETITIONS.map(c => ({ ...c, status: c.id === 1 ? compStatus : c.status }));

  return (
    <div style={{ padding: 20, display: 'flex', flexDirection: 'column', gap: 12 }}>
      {/* Filter bar */}
      <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
        {filters.map(f => (
          <button key={f} onClick={() => setFilter(f)} style={{
            padding: '5px 12px', borderRadius: 7, border: `1px solid ${t.border}`,
            background: filter === f ? '#2563EB' : t.card,
            color: filter === f ? '#fff' : t.fgMuted,
            fontSize: 11, fontWeight: filter === f ? 600 : 400,
            cursor: 'pointer', fontFamily: 'inherit',
          }}>{f}</button>
        ))}
      </div>
      {/* Cards */}
      {filtered.map(comp => {
        const s = COMP_STATUS[comp.status] || COMP_STATUS.DRAFT;
        return (
          <div key={comp.id} style={{
            background: t.card, border: `1px solid ${t.border}`,
            borderRadius: 10, padding: '14px 16px',
            boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 6 }}>
              <div style={{ fontSize: 14, fontWeight: 600, color: t.fg }}>{comp.name}</div>
              <span style={{ background: s.bg, color: s.color, padding: '2px 8px', borderRadius: 6, fontSize: 10, fontWeight: 600, whiteSpace: 'nowrap', marginLeft: 8 }}>{s.label}</span>
            </div>
            <div style={{ fontSize: 11, color: t.fgMuted, marginBottom: 2, display: 'flex', alignItems: 'center', gap: 5 }}>
              <AdminIcon name="Calendar" size={11} color={t.fgSubtle} />
              {comp.dateRange}
            </div>
            <div style={{ fontSize: 11, color: t.fgMuted, marginBottom: 10, display: 'flex', alignItems: 'center', gap: 5 }}>
              <AdminIcon name="ClipboardList" size={11} color={t.fgSubtle} />
              신청 기간: {comp.regRange} &nbsp;·&nbsp;
              <AdminIcon name="Users" size={11} color={t.fgSubtle} />
              참가자 {comp.participants}명
            </div>
            <div style={{ display: 'flex', gap: 6, justifyContent: 'flex-end' }}>
              <Btn variant="secondary" small dark={dark}>상세 보기</Btn>
              {comp.status === 'DRAFT' ? (
                <Btn variant="secondary" small dark={dark} icon="Pencil">편집</Btn>
              ) : (
                <Btn variant="secondary" small dark={dark} icon="Users">신청 관리</Btn>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
}

/* ════════════════════════════════════════════════════════════════
   SCREEN 2 — 대회 생성/편집
═══════════════════════════════════════════════════════════════ */
function FormLabel({ children, t }) {
  return <div style={{ fontSize: 11, fontWeight: 500, color: t.fgMuted, marginBottom: 4 }}>{children}</div>;
}
function FormInput({ placeholder, value, t, wide }) {
  return (
    <input
      readOnly
      defaultValue={value || ''}
      placeholder={placeholder}
      style={{
        width: wide ? '100%' : 'auto',
        padding: '7px 10px', border: `1px solid ${t.border}`,
        borderRadius: 7, fontSize: 12, fontFamily: 'inherit',
        color: t.fg, background: t.card, outline: 'none',
      }}
    />
  );
}
function SectionTitle({ children, t }) {
  return (
    <div style={{ fontSize: 12, fontWeight: 600, color: t.fgMuted, textTransform: 'uppercase', letterSpacing: '0.06em', paddingBottom: 8, borderBottom: `1px solid ${t.border}`, marginBottom: 12 }}>
      {children}
    </div>
  );
}

function CompetitionEditScreen({ dark }) {
  const t = getTokens(dark);
  return (
    <div style={{ padding: 20, display: 'flex', flexDirection: 'column', gap: 16, maxWidth: 680 }}>
      <SectionTitle t={t}>기본 정보</SectionTitle>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        <div>
          <FormLabel t={t}>대회명 *</FormLabel>
          <FormInput value="2026 Seoul CrossFit Open" t={t} wide />
        </div>
        <div>
          <FormLabel t={t}>설명 (선택)</FormLabel>
          <textarea readOnly defaultValue="모든 레벨의 참가자를 위한 열린 크로스핏 대회입니다. RXD와 SCALED 두 부문으로 운영됩니다." style={{ width: '100%', padding: '7px 10px', border: `1px solid ${t.border}`, borderRadius: 7, fontSize: 12, fontFamily: 'inherit', color: t.fg, background: t.card, resize: 'none', height: 64, outline: 'none', lineHeight: 1.5 }} />
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
          <div>
            <FormLabel t={t}>대회 시작일 *</FormLabel>
            <FormInput value="2026-05-01" t={t} wide />
          </div>
          <div>
            <FormLabel t={t}>대회 종료일 *</FormLabel>
            <FormInput value="2026-05-10" t={t} wide />
          </div>
        </div>
        <div>
          <FormLabel t={t}>배너 이미지 (선택)</FormLabel>
          <div style={{ border: `2px dashed ${t.border}`, borderRadius: 8, padding: '14px', textAlign: 'center', color: t.fgSubtle, fontSize: 11 }}>
            <AdminIcon name="ImagePlus" size={20} color={t.fgSubtle} />
            <div style={{ marginTop: 6 }}>이미지 업로드 또는 URL 입력</div>
          </div>
        </div>
      </div>

      <SectionTitle t={t}>참가 신청 기간</SectionTitle>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
        <div>
          <FormLabel t={t}>신청 시작</FormLabel>
          <FormInput value="2026-04-01 00:00" t={t} wide />
        </div>
        <div>
          <FormLabel t={t}>신청 마감</FormLabel>
          <FormInput value="2026-04-30 23:59" t={t} wide />
        </div>
      </div>

      <SectionTitle t={t}>참가비 & 계좌 정보</SectionTitle>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <div style={{ flex: 1 }}>
            <FormLabel t={t}>참가비 (원)</FormLabel>
            <FormInput value="50,000" t={t} wide />
          </div>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
          <div>
            <FormLabel t={t}>은행명</FormLabel>
            <FormInput value="국민은행" t={t} wide />
          </div>
          <div>
            <FormLabel t={t}>계좌번호</FormLabel>
            <FormInput value="123-456-789012" t={t} wide />
          </div>
        </div>
        <div>
          <FormLabel t={t}>예금주</FormLabel>
          <FormInput value="박주최" t={t} wide />
        </div>
      </div>

      <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end', paddingTop: 4 }}>
        <Btn variant="ghost" dark={dark}>취소</Btn>
        <Btn variant="primary" dark={dark} icon="ChevronRight">저장 후 Stage 구성</Btn>
      </div>
    </div>
  );
}

/* ════════════════════════════════════════════════════════════════
   SCREEN 3 — 대회 상세 (구성 탭)
═══════════════════════════════════════════════════════════════ */
function CompetitionDetailScreen({ dark, compStatus }) {
  const t = getTokens(dark);
  const [tab, setTab] = useState('config');
  const [open1, setOpen1] = useState(true);
  const [open2, setOpen2] = useState(false);
  const tabs = [
    { id: 'config', label: '구성' },
    { id: 'reg', label: '신청 관리' },
    { id: 'scores', label: '기록 판독' },
    { id: 'lb', label: '리더보드' },
  ];
  const s = COMP_STATUS[compStatus] || COMP_STATUS.DRAFT;

  return (
    <div>
      {/* Comp header */}
      <div style={{ padding: '16px 20px 0', background: t.card, borderBottom: `1px solid ${t.border}` }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 4 }}>
          <div>
            <div style={{ fontSize: 18, fontWeight: 700, color: t.fg, marginBottom: 2 }}>2026 Seoul CrossFit Open</div>
            <div style={{ fontSize: 11, color: t.fgMuted, display: 'flex', gap: 8, flexWrap: 'wrap' }}>
              <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}><AdminIcon name="Calendar" size={11} color={t.fgSubtle} />2026.05.01 — 2026.05.10</span>
              <span>신청 2026.04.01 — 2026.04.30</span>
            </div>
          </div>
          <div style={{ display: 'flex', gap: 6, alignItems: 'center', flexShrink: 0 }}>
            <span style={{ background: s.bg, color: s.color, padding: '3px 10px', borderRadius: 6, fontSize: 10, fontWeight: 600 }}>{s.label}</span>
            <Btn variant="ghost" small dark={dark} icon="Pencil">대회 편집</Btn>
          </div>
        </div>
        {/* Tabs */}
        <div style={{ display: 'flex', gap: 0, marginTop: 12 }}>
          {tabs.map(tb => (
            <button key={tb.id} onClick={() => setTab(tb.id)} style={{
              padding: '8px 16px', border: 'none', background: 'transparent',
              borderBottom: tab === tb.id ? '2px solid #2563EB' : '2px solid transparent',
              color: tab === tb.id ? '#2563EB' : t.fgMuted,
              fontSize: 12, fontWeight: tab === tb.id ? 600 : 400,
              cursor: 'pointer', fontFamily: 'inherit',
              transition: 'color 150ms',
            }}>{tb.label}</button>
          ))}
        </div>
      </div>

      {/* Tab content */}
      <div style={{ padding: 20 }}>
        {tab === 'config' && (
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
              <div style={{ fontSize: 13, fontWeight: 600, color: t.fg }}>Stage 구성</div>
              <Btn variant="primary" small dark={dark} icon="Plus">Stage 추가</Btn>
            </div>

            {/* Stage 1 — 예선 */}
            <div style={{ border: `1px solid ${t.border}`, borderRadius: 10, marginBottom: 10, overflow: 'hidden' }}>
              <div
                onClick={() => setOpen1(!open1)}
                style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '10px 14px', background: t.muted, cursor: 'pointer' }}
              >
                <AdminIcon name={open1 ? 'ChevronDown' : 'ChevronRight'} size={14} color={t.fgMuted} />
                <span style={{ fontSize: 12, fontWeight: 600, color: t.fg }}>예선 (QUALIFIER)</span>
                <span style={{ fontSize: 11, color: t.fgMuted }}>·&nbsp; 온라인 (ONLINE)</span>
                <span style={{ fontSize: 11, color: t.fgMuted, marginLeft: 4 }}>·&nbsp; 2026.04.10 — 2026.04.20</span>
                <div style={{ marginLeft: 'auto', display: 'flex', gap: 6 }}>
                  <Btn variant="ghost" small dark={dark} icon="Pencil">편집</Btn>
                  <Btn variant="ghost" small dark={dark} icon="Users">본선 진출 선별</Btn>
                </div>
              </div>
              {open1 && (
                <div style={{ padding: '12px 14px', display: 'flex', flexDirection: 'column', gap: 8 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 }}>
                    <span style={{ fontSize: 11, color: t.fgMuted, fontWeight: 500, textTransform: 'uppercase', letterSpacing: '0.04em' }}>Event 목록</span>
                    <Btn variant="secondary" small dark={dark} icon="Plus">Event 추가</Btn>
                  </div>
                  {[
                    { num: 1, name: 'Event 1 — 21.1', type: 'INDIVIDUAL', gender: 'MEN + WOMEN', scale: 'RXD / SCALED', wod: 'For Time', cap: '제한 없음', deadline: '2026.04.20 23:59' },
                    { num: 2, name: 'Event 2 — 21.2', type: 'TEAM', gender: 'MIXED', scale: 'RXD / SCALED', wod: 'AMRAP', cap: '20분', deadline: '2026.04.20 23:59' },
                    { num: 3, name: 'Event 3 — 21.3', type: 'INDIVIDUAL', gender: 'MEN + WOMEN', scale: 'RXD / SCALED', wod: 'For Time', cap: '15분', deadline: '2026.04.20 23:59' },
                  ].map(ev => (
                    <div key={ev.num} style={{ border: `1px solid ${t.border}`, borderRadius: 8, padding: '10px 12px', background: t.card }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                        <div>
                          <div style={{ fontSize: 12, fontWeight: 600, color: t.fg, marginBottom: 3 }}>#{ev.num}&nbsp; {ev.name}</div>
                          <div style={{ display: 'flex', gap: 4, flexWrap: 'wrap', marginBottom: 3 }}>
                            {[ev.type, ev.gender, ev.scale].map(tag => (
                              <span key={tag} style={{ background: t.muted, color: t.fgMuted, padding: '1px 6px', borderRadius: 4, fontSize: 10, fontWeight: 500 }}>{tag}</span>
                            ))}
                          </div>
                          <div style={{ fontSize: 11, color: t.fgMuted }}>
                            {ev.wod} &nbsp;·&nbsp; {ev.cap} &nbsp;·&nbsp; 마감: {ev.deadline}
                          </div>
                        </div>
                        <div style={{ display: 'flex', gap: 4, flexShrink: 0 }}>
                          <Btn variant="ghost" small dark={dark} icon="Pencil">편집</Btn>
                          <Btn variant="ghost" small dark={dark} icon="Trash2">삭제</Btn>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Stage 2 — 본선 */}
            <div style={{ border: `1px solid ${t.border}`, borderRadius: 10, overflow: 'hidden' }}>
              <div
                onClick={() => setOpen2(!open2)}
                style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '10px 14px', background: t.muted, cursor: 'pointer' }}
              >
                <AdminIcon name={open2 ? 'ChevronDown' : 'ChevronRight'} size={14} color={t.fgMuted} />
                <span style={{ fontSize: 12, fontWeight: 600, color: t.fg }}>본선 (FINAL)</span>
                <span style={{ fontSize: 11, color: t.fgMuted }}>·&nbsp; 오프라인 (OFFLINE)</span>
                <span style={{ fontSize: 11, color: t.fgMuted, marginLeft: 4 }}>·&nbsp; 2026.05.01 — 2026.05.01</span>
                <div style={{ marginLeft: 'auto' }}>
                  <Btn variant="ghost" small dark={dark} icon="Pencil">편집</Btn>
                </div>
              </div>
              {open2 && (
                <div style={{ padding: '12px 14px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                    <span style={{ fontSize: 11, color: t.fgMuted, fontWeight: 500, textTransform: 'uppercase', letterSpacing: '0.04em' }}>Event 목록</span>
                    <Btn variant="secondary" small dark={dark} icon="Plus">Event 추가</Btn>
                  </div>
                  <div style={{ fontSize: 12, color: t.fgSubtle, textAlign: 'center', padding: '12px 0' }}>아직 이벤트가 없습니다.</div>
                </div>
              )}
            </div>
          </div>
        )}
        {tab !== 'config' && (
          <div style={{ textAlign: 'center', color: t.fgSubtle, fontSize: 13, padding: '40px 0' }}>
            <AdminIcon name={tab === 'reg' ? 'Users' : tab === 'scores' ? 'ClipboardList' : 'BarChart2'} size={32} color={t.border} />
            <div style={{ marginTop: 8 }}>{tab === 'reg' ? '신청 관리' : tab === 'scores' ? '기록 판독' : '리더보드'} 탭 선택됨</div>
          </div>
        )}
      </div>
    </div>
  );
}

/* ════════════════════════════════════════════════════════════════
   SCREEN 4 — 신청 관리
═══════════════════════════════════════════════════════════════ */
const REGISTRATIONS = [
  { id: 1, name: '홍길동',  type: '개인전', scale: 'RXD',    date: '04.01', status: 'PENDING',   payerNote: '홍길동 0401', expanded: false },
  { id: 2, name: 'Team Alpha', type: '팀전', scale: 'RXD', date: '04.02', status: 'PENDING',   payerNote: '김대표 팀 0402', isTeam: true, members: ['김대표 (남, 리더)', '박팀원 (남)', '이팀원 (여)'] },
  { id: 3, name: '이영희',  type: '개인전', scale: 'SCALED', date: '03.30', status: 'CONFIRMED', payerNote: '' },
  { id: 4, name: '최민준',  type: '개인전', scale: 'RXD',    date: '04.03', status: 'PENDING',   payerNote: '최민준' },
  { id: 5, name: '강수연',  type: '개인전', scale: 'SCALED', date: '04.04', status: 'REJECTED',  payerNote: '' },
  { id: 6, name: 'Team Blaze', type: '팀전', scale: 'SCALED', date: '04.05', status: 'CONFIRMED', payerNote: '오팀장 0405', isTeam: true, members: ['오팀장 (남, 리더)', '나연 (여)'] },
];

function RegistrationManageScreen({ dark }) {
  const t = getTokens(dark);
  const [filter, setFilter] = useState('전체');
  const [expanded, setExpanded] = useState({});
  const filters = ['전체', '대기', '확인됨', '거절됨'];
  const counts = { PENDING: 3, CONFIRMED: 2, REJECTED: 1 };
  const filtered = filter === '전체' ? REGISTRATIONS
    : filter === '대기' ? REGISTRATIONS.filter(r => r.status === 'PENDING')
    : filter === '확인됨' ? REGISTRATIONS.filter(r => r.status === 'CONFIRMED')
    : REGISTRATIONS.filter(r => r.status === 'REJECTED');

  return (
    <div style={{ padding: 20 }}>
      {/* Stats row */}
      <div style={{ display: 'flex', gap: 8, marginBottom: 14 }}>
        {[
          { label: '전체 신청', val: 6, color: t.fg },
          { label: 'CONFIRMED', val: counts.CONFIRMED, color: '#22C55E' },
          { label: 'PENDING', val: counts.PENDING, color: '#EAB308' },
          { label: 'REJECTED', val: counts.REJECTED, color: '#E53E3E' },
        ].map(s => (
          <div key={s.label} style={{ flex: 1, background: t.card, border: `1px solid ${t.border}`, borderRadius: 8, padding: '8px 10px', textAlign: 'center' }}>
            <div style={{ fontSize: 18, fontWeight: 700, color: s.color, fontFamily: 'Geist Mono Variable, monospace' }}>{s.val}</div>
            <div style={{ fontSize: 9, color: t.fgMuted, marginTop: 2 }}>{s.label}</div>
          </div>
        ))}
      </div>

      {/* Filter + bulk actions */}
      <div style={{ display: 'flex', gap: 6, marginBottom: 10, alignItems: 'center' }}>
        {filters.map(f => (
          <button key={f} onClick={() => setFilter(f)} style={{
            padding: '4px 10px', borderRadius: 6, border: `1px solid ${t.border}`,
            background: filter === f ? '#2563EB' : t.card, color: filter === f ? '#fff' : t.fgMuted,
            fontSize: 11, fontWeight: filter === f ? 600 : 400, cursor: 'pointer', fontFamily: 'inherit',
          }}>{f}</button>
        ))}
        <div style={{ marginLeft: 'auto', display: 'flex', gap: 6 }}>
          <Btn variant="primary" small dark={dark}>일괄 승인</Btn>
          <Btn variant="danger" small dark={dark}>일괄 거절</Btn>
        </div>
      </div>

      {/* Table */}
      <div style={{ background: t.card, border: `1px solid ${t.border}`, borderRadius: 10, overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ background: t.muted, borderBottom: `1px solid ${t.border}` }}>
              {['☐', '이름', '타입', '스케일', '신청일', '상태', '액션'].map(h => (
                <th key={h} style={{ padding: '8px 12px', textAlign: 'left', fontSize: 10, fontWeight: 600, color: t.fgMuted, letterSpacing: '0.04em', whiteSpace: 'nowrap' }}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {filtered.map(r => {
              const sc = REG_STATUS[r.status];
              const isExp = expanded[r.id];
              return (
                <React.Fragment key={r.id}>
                  <tr style={{ borderBottom: `1px solid ${t.border}`, background: t.card }}>
                    <td style={{ padding: '9px 12px' }}><input type="checkbox" /></td>
                    <td style={{ padding: '9px 12px' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        {r.isTeam && (
                          <button onClick={() => setExpanded(e => ({ ...e, [r.id]: !e[r.id] }))} style={{ border: 'none', background: 'none', cursor: 'pointer', color: t.fgMuted, fontSize: 10, display: 'flex', alignItems: 'center', gap: 2 }}>
                            <AdminIcon name={isExp ? 'ChevronDown' : 'ChevronRight'} size={12} color={t.fgMuted} />
                          </button>
                        )}
                        <span style={{ fontSize: 12, fontWeight: 500, color: t.fg }}>{r.name}</span>
                      </div>
                      {r.payerNote && <div style={{ fontSize: 10, color: t.fgMuted, marginTop: 1 }}>입금자명: {r.payerNote}</div>}
                    </td>
                    <td style={{ padding: '9px 12px' }}>
                      <span style={{ background: r.type === '팀전' ? '#DBEAFE' : t.muted, color: r.type === '팀전' ? '#1E40AF' : t.fgMuted, padding: '2px 6px', borderRadius: 4, fontSize: 10, fontWeight: 500 }}>{r.type}</span>
                    </td>
                    <td style={{ padding: '9px 12px', fontSize: 11, color: t.fgMuted }}>{r.scale}</td>
                    <td style={{ padding: '9px 12px', fontSize: 11, color: t.fgMuted, fontFamily: 'Geist Mono Variable, monospace' }}>{r.date}</td>
                    <td style={{ padding: '9px 12px' }}>
                      <span style={{ background: sc.bg, color: sc.color, padding: '2px 8px', borderRadius: 6, fontSize: 10, fontWeight: 600 }}>{sc.label}</span>
                    </td>
                    <td style={{ padding: '9px 12px' }}>
                      <div style={{ display: 'flex', gap: 4 }}>
                        {r.status === 'PENDING' && <><Btn variant="primary" small dark={dark}>승인</Btn><Btn variant="danger" small dark={dark}>거절</Btn></>}
                        {r.status === 'CONFIRMED' && <Btn variant="ghost" small dark={dark}>거절로 변경</Btn>}
                        {r.status === 'REJECTED' && <Btn variant="primary" small dark={dark}>승인으로 변경</Btn>}
                      </div>
                    </td>
                  </tr>
                  {r.isTeam && isExp && (
                    <tr style={{ background: t.muted }}>
                      <td></td>
                      <td colSpan={6} style={{ padding: '8px 12px' }}>
                        {r.members.map((m, i) => (
                          <div key={i} style={{ fontSize: 11, color: t.fgMuted, padding: '2px 0', display: 'flex', alignItems: 'center', gap: 4 }}>
                            <AdminIcon name="User" size={11} color={t.fgSubtle} />
                            {m}
                          </div>
                        ))}
                      </td>
                    </tr>
                  )}
                </React.Fragment>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}

/* ════════════════════════════════════════════════════════════════
   SCREEN 5 — 기록 판독
═══════════════════════════════════════════════════════════════ */
const SCORES_DATA = [
  { id: 1, name: '홍길동',   gender: '남', score: '8:30',  scale: 'RXD',    video: true,  status: 'SUBMITTED' },
  { id: 2, name: '김철수',   gender: '남', score: '9:15',  scale: 'RXD',    video: true,  status: 'UNDER_REVIEW' },
  { id: 3, name: '이영희',   gender: '여', score: 'DNF',   scale: 'SCALED', video: false, status: 'SUBMITTED' },
  { id: 4, name: 'Team Alpha',gender: 'MIX',score: '14:50', scale: 'RXD',   video: true,  status: 'APPROVED' },
  { id: 5, name: '박민수',   gender: '남', score: '10:02', scale: 'RXD',    video: true,  status: 'ADJUSTED' },
  { id: 6, name: '최지수',   gender: '여', score: '11:44', scale: 'SCALED', video: true,  status: 'REJECTED' },
];

function ScoreJudgeScreen({ dark }) {
  const t = getTokens(dark);
  const [selEvent, setSelEvent] = useState('Event 1 — 21.1');
  const [filterStatus, setFilterStatus] = useState('전체');
  const [dialog, setDialog] = useState(null);
  const [verdict, setVerdict] = useState('승인');
  const events = ['Event 1 — 21.1', 'Event 2 — 21.2', 'Event 3 — 21.3'];
  const statusFilters = ['전체', '제출됨', '검토중', '승인', '조정됨', '거절됨'];

  return (
    <div style={{ padding: 20 }}>
      {/* Event selector */}
      <div style={{ display: 'flex', gap: 8, marginBottom: 12, alignItems: 'center', flexWrap: 'wrap' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <span style={{ fontSize: 11, color: t.fgMuted, fontWeight: 500 }}>Event</span>
          <select defaultValue={selEvent} onChange={e => setSelEvent(e.target.value)} style={{ padding: '5px 10px', border: `1px solid ${t.border}`, borderRadius: 7, fontSize: 11, fontFamily: 'inherit', color: t.fg, background: t.card, cursor: 'pointer' }}>
            {events.map(ev => <option key={ev}>{ev}</option>)}
          </select>
        </div>
        <div style={{ display: 'flex', gap: 4, flexWrap: 'wrap' }}>
          {statusFilters.map(f => (
            <button key={f} onClick={() => setFilterStatus(f)} style={{
              padding: '4px 9px', borderRadius: 6, border: `1px solid ${t.border}`,
              background: filterStatus === f ? '#2563EB' : t.card, color: filterStatus === f ? '#fff' : t.fgMuted,
              fontSize: 10, fontWeight: filterStatus === f ? 600 : 400, cursor: 'pointer', fontFamily: 'inherit',
            }}>{f}</button>
          ))}
        </div>
      </div>

      {/* Table */}
      <div style={{ background: t.card, border: `1px solid ${t.border}`, borderRadius: 10, overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ background: t.muted, borderBottom: `1px solid ${t.border}` }}>
              {['참가자', '성별', '기록', '스케일', '영상', '상태', '판독'].map(h => (
                <th key={h} style={{ padding: '8px 12px', textAlign: 'left', fontSize: 10, fontWeight: 600, color: t.fgMuted, letterSpacing: '0.04em' }}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {SCORES_DATA.map(row => {
              const sc = SCORE_STATUS[row.status];
              return (
                <tr key={row.id} style={{ borderBottom: `1px solid ${t.border}`, background: t.card }}>
                  <td style={{ padding: '9px 12px', fontSize: 12, fontWeight: 500, color: t.fg }}>{row.name}</td>
                  <td style={{ padding: '9px 12px', fontSize: 11, color: t.fgMuted }}>{row.gender}</td>
                  <td style={{ padding: '9px 12px', fontSize: 13, fontWeight: 600, color: t.fg, fontFamily: 'Geist Mono Variable, monospace' }}>{row.score}</td>
                  <td style={{ padding: '9px 12px' }}>
                    <span style={{ background: row.scale === 'RXD' ? '#1A202C' : t.muted, color: row.scale === 'RXD' ? '#fff' : t.fgMuted, padding: '2px 6px', borderRadius: 4, fontSize: 10, fontWeight: 600 }}>{row.scale}</span>
                  </td>
                  <td style={{ padding: '9px 12px' }}>
                    {row.video
                      ? <button style={{ fontSize: 10, color: '#2563EB', background: 'none', border: `1px solid #DBEAFE`, borderRadius: 5, padding: '2px 7px', cursor: 'pointer', fontFamily: 'inherit', display: 'flex', alignItems: 'center', gap: 3 }}><AdminIcon name="Play" size={10} color="#2563EB" />영상 보기</button>
                      : <span style={{ fontSize: 10, color: t.fgSubtle }}>—</span>}
                  </td>
                  <td style={{ padding: '9px 12px' }}>
                    <span style={{ background: sc.bg, color: sc.color, border: sc.border ? `1px solid ${sc.border}` : 'none', padding: '2px 8px', borderRadius: 6, fontSize: 10, fontWeight: 600 }}>{sc.label}</span>
                  </td>
                  <td style={{ padding: '9px 12px' }}>
                    <button onClick={() => setDialog(row)} style={{ fontSize: 10, background: '#EFF6FF', color: '#2563EB', border: '1px solid #DBEAFE', borderRadius: 5, padding: '3px 8px', cursor: 'pointer', fontFamily: 'inherit' }}>판독 →</button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* Dialog */}
      {dialog && (
        <div style={{ position: 'absolute', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 50 }}>
          <div style={{ background: t.card, border: `1px solid ${t.border}`, borderRadius: 12, width: 420, padding: 20, boxShadow: '0 10px 40px rgba(0,0,0,0.15)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 }}>
              <div style={{ fontSize: 14, fontWeight: 700, color: t.fg }}>기록 판독 — {dialog.name} · Event 1</div>
              <button onClick={() => setDialog(null)} style={{ border: 'none', background: 'none', cursor: 'pointer', color: t.fgMuted }}>✕</button>
            </div>
            <div style={{ background: t.muted, borderRadius: 8, padding: '10px 12px', marginBottom: 12, display: 'flex', flexDirection: 'column', gap: 4 }}>
              {[
                ['제출된 기록', `${dialog.score}  (For Time, ${dialog.scale})`],
                ['제출자', dialog.name],
                ['제출 시각', '2026.04.15 14:23'],
              ].map(([k, v]) => (
                <div key={k} style={{ display: 'flex', gap: 8, fontSize: 12 }}>
                  <span style={{ color: t.fgMuted, width: 70 }}>{k}</span>
                  <span style={{ color: t.fg, fontWeight: 500 }}>{v}</span>
                </div>
              ))}
            </div>
            <div style={{ marginBottom: 12 }}>
              <FormLabel t={t}>YouTube 영상</FormLabel>
              <div style={{ display: 'flex', gap: 6, alignItems: 'center', border: `1px solid ${t.border}`, borderRadius: 7, padding: '6px 10px', background: t.muted }}>
                <span style={{ fontSize: 11, color: t.fgMuted, flex: 1 }}>https://youtu.be/xxxxx_sample</span>
                <button style={{ fontSize: 10, color: '#2563EB', background: 'none', border: 'none', cursor: 'pointer', fontFamily: 'inherit', display: 'flex', alignItems: 'center', gap: 3 }}><AdminIcon name="ExternalLink" size={11} color="#2563EB" />새 탭</button>
              </div>
            </div>
            <div style={{ marginBottom: 12 }}>
              <FormLabel t={t}>판독 결과 *</FormLabel>
              <div style={{ display: 'flex', gap: 6 }}>
                {['승인', '조정', '거절'].map(v => (
                  <button key={v} onClick={() => setVerdict(v)} style={{
                    flex: 1, padding: '7px', borderRadius: 7, cursor: 'pointer', fontFamily: 'inherit', fontSize: 12, fontWeight: 500,
                    border: verdict === v ? '2px solid #2563EB' : `1px solid ${t.border}`,
                    background: verdict === v ? '#EFF6FF' : t.card,
                    color: verdict === v ? '#2563EB' : t.fgMuted,
                  }}>{v}</button>
                ))}
              </div>
            </div>
            {verdict === '조정' && (
              <div style={{ marginBottom: 12 }}>
                <FormLabel t={t}>조정 기록</FormLabel>
                <div style={{ display: 'flex', gap: 6, marginBottom: 6 }}>
                  <FormInput value="8" t={t} /><span style={{ fontSize: 11, color: t.fgMuted, alignSelf: 'center' }}>분</span>
                  <FormInput value="00" t={t} /><span style={{ fontSize: 11, color: t.fgMuted, alignSelf: 'center' }}>초</span>
                </div>
                <FormLabel t={t}>조정 메모 (필수)</FormLabel>
                <textarea readOnly defaultValue="타이머 오류 확인, 8:00으로 조정" style={{ width: '100%', padding: '6px 9px', border: `1px solid ${t.border}`, borderRadius: 7, fontSize: 11, fontFamily: 'inherit', color: t.fg, background: t.card, resize: 'none', height: 48, outline: 'none' }} />
              </div>
            )}
            {verdict === '거절' && (
              <div style={{ marginBottom: 12 }}>
                <FormLabel t={t}>거절 사유 (필수)</FormLabel>
                <textarea readOnly defaultValue="영상 링크가 비공개 설정되어 확인 불가" style={{ width: '100%', padding: '6px 9px', border: `1px solid ${t.border}`, borderRadius: 7, fontSize: 11, fontFamily: 'inherit', color: t.fg, background: t.card, resize: 'none', height: 48, outline: 'none' }} />
              </div>
            )}
            <div style={{ display: 'flex', gap: 6, justifyContent: 'flex-end' }}>
              <Btn variant="ghost" dark={dark} onClick={() => setDialog(null)}>취소</Btn>
              <Btn variant="primary" dark={dark}>판독 완료</Btn>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

/* ════════════════════════════════════════════════════════════════
   SCREEN 6 — 리더보드 프로젝션
═══════════════════════════════════════════════════════════════ */
const LB_DATA = [
  { rank: 1, name: '홍길동', ev1: '1pt', ev2: '2pt', ev3: '1pt', total: '4pt', flag: '★' },
  { rank: 2, name: '김철수', ev1: '2pt', ev2: '1pt', ev3: '3pt', total: '6pt', flag: '' },
  { rank: 3, name: '박민수', ev1: '3pt', ev2: '3pt', ev3: '2pt', total: '8pt', flag: '' },
  { rank: 4, name: '최지원', ev1: '4pt', ev2: '4pt', ev3: '—',   total: '8pt', flag: '⚑' },
  { rank: 5, name: '이수빈', ev1: '5pt', ev2: '5pt', ev3: '4pt', total: '14pt', flag: '' },
  { rank: 6, name: '강동원', ev1: '6pt', ev2: '6pt', ev3: '5pt', total: '17pt', flag: '' },
];

function LeaderboardScreen({ dark }) {
  const t = getTokens(dark);
  const [lbTab, setLbTab] = useState('종합');
  const [stage, setStage] = useState('예선');
  const [type, setType] = useState('개인전');
  const [gender, setGender] = useState('전체');
  const [scale, setScale] = useState('전체');
  const lbTabs = ['종합', 'Event 1', 'Event 2', 'Event 3'];
  const rankColor = { 1: '#F59E0B', 2: '#9CA3AF', 3: '#B45309' };

  return (
    <div style={{ padding: 20 }}>
      {/* Filter rows */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginBottom: 14 }}>
        <div style={{ display: 'flex', gap: 4, flexWrap: 'wrap' }}>
          {lbTabs.map(tb => (
            <button key={tb} onClick={() => setLbTab(tb)} style={{
              padding: '5px 12px', borderRadius: 6, border: `1px solid ${t.border}`,
              background: lbTab === tb ? '#2563EB' : t.card, color: lbTab === tb ? '#fff' : t.fgMuted,
              fontSize: 11, fontWeight: lbTab === tb ? 600 : 400, cursor: 'pointer', fontFamily: 'inherit',
            }}>{tb}</button>
          ))}
        </div>
        <div style={{ display: 'flex', gap: 4, flexWrap: 'wrap', alignItems: 'center' }}>
          <span style={{ fontSize: 10, color: t.fgSubtle, marginRight: 2 }}>Stage:</span>
          {['예선', '본선'].map(v => <button key={v} onClick={() => setStage(v)} style={{ padding: '4px 9px', borderRadius: 6, border: `1px solid ${t.border}`, background: stage === v ? t.primary : t.card, color: stage === v ? '#fff' : t.fgMuted, fontSize: 10, cursor: 'pointer', fontFamily: 'inherit' }}>{v}</button>)}
          <span style={{ fontSize: 10, color: t.fgSubtle, marginLeft: 6, marginRight: 2 }}>타입:</span>
          {['개인전', '팀전'].map(v => <button key={v} onClick={() => setType(v)} style={{ padding: '4px 9px', borderRadius: 6, border: `1px solid ${t.border}`, background: type === v ? t.primary : t.card, color: type === v ? '#fff' : t.fgMuted, fontSize: 10, cursor: 'pointer', fontFamily: 'inherit' }}>{v}</button>)}
          <span style={{ fontSize: 10, color: t.fgSubtle, marginLeft: 6, marginRight: 2 }}>성별:</span>
          {['전체', '남', '여'].map(v => <button key={v} onClick={() => setGender(v)} style={{ padding: '4px 9px', borderRadius: 6, border: `1px solid ${t.border}`, background: gender === v ? t.primary : t.card, color: gender === v ? '#fff' : t.fgMuted, fontSize: 10, cursor: 'pointer', fontFamily: 'inherit' }}>{v}</button>)}
          <span style={{ fontSize: 10, color: t.fgSubtle, marginLeft: 6, marginRight: 2 }}>스케일:</span>
          {['전체', 'RXD', 'SCALED'].map(v => <button key={v} onClick={() => setScale(v)} style={{ padding: '4px 9px', borderRadius: 6, border: `1px solid ${t.border}`, background: scale === v ? t.primary : t.card, color: scale === v ? '#fff' : t.fgMuted, fontSize: 10, cursor: 'pointer', fontFamily: 'inherit' }}>{v}</button>)}
        </div>
      </div>

      {/* Table */}
      <div style={{ background: t.card, border: `1px solid ${t.border}`, borderRadius: 10, overflow: 'hidden' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px 14px', background: dark ? '#1A2535' : '#1A202C' }}>
          <span style={{ fontSize: 12, fontWeight: 600, color: '#fff' }}>종합 순위 — 개인전 · 전체 · {scale}</span>
          <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
            <span style={{ fontSize: 10, color: '#64748B' }}>마지막 갱신 14:23</span>
            <div style={{ display: 'flex', alignItems: 'center', gap: 4, background: 'rgba(34,197,94,0.15)', padding: '3px 8px', borderRadius: 9999 }}>
              <div style={{ width: 6, height: 6, borderRadius: '50%', background: '#22C55E' }} />
              <span style={{ fontSize: 10, color: '#22C55E', fontWeight: 500 }}>실시간 연결됨</span>
            </div>
            <button style={{ fontSize: 10, background: 'rgba(255,255,255,0.1)', color: '#fff', border: '1px solid rgba(255,255,255,0.2)', borderRadius: 5, padding: '3px 8px', cursor: 'pointer', fontFamily: 'inherit', display: 'flex', alignItems: 'center', gap: 4 }}>
              <AdminIcon name="Maximize2" size={10} color="#fff" />전체화면
            </button>
          </div>
        </div>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ background: t.muted, borderBottom: `1px solid ${t.border}` }}>
              {['#', '이름', 'Ev.1', 'Ev.2', 'Ev.3', '합계'].map(h => (
                <th key={h} style={{ padding: '8px 12px', textAlign: h === '#' || h === '합계' ? 'center' : 'left', fontSize: 10, fontWeight: 600, color: t.fgMuted, letterSpacing: '0.04em' }}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {LB_DATA.map(row => (
              <tr key={row.rank} style={{ borderBottom: `1px solid ${t.border}`, background: t.card }}>
                <td style={{ padding: '10px 12px', textAlign: 'center' }}>
                  <span style={{ fontSize: 14, fontWeight: 700, color: rankColor[row.rank] || t.fgMuted, fontFamily: 'Geist Mono Variable, monospace' }}>{row.rank}</span>
                </td>
                <td style={{ padding: '10px 12px' }}>
                  <span style={{ fontSize: 13, fontWeight: 500, color: t.fg }}>{row.name}</span>
                  {row.flag && <span style={{ marginLeft: 5, fontSize: 12, color: row.flag === '★' ? '#F59E0B' : '#94A3B8' }}>{row.flag}</span>}
                </td>
                {[row.ev1, row.ev2, row.ev3].map((v, i) => (
                  <td key={i} style={{ padding: '10px 12px', fontSize: 12, color: v === '—' ? t.fgSubtle : t.fgMuted, fontFamily: 'Geist Mono Variable, monospace' }}>{v}</td>
                ))}
                <td style={{ padding: '10px 12px', textAlign: 'center' }}>
                  <span style={{ fontSize: 14, fontWeight: 700, color: rankColor[row.rank] || t.fg, fontFamily: 'Geist Mono Variable, monospace' }}>{row.total}</span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div style={{ marginTop: 6, display: 'flex', gap: 12, fontSize: 10, color: t.fgSubtle }}>
        <span>★ 타이브레이커로 상위 배정</span>
        <span>⚑ 주최자 수동 순위 override</span>
        <span>— 기록 없음</span>
      </div>
    </div>
  );
}

/* ─── Admin Shell ───────────────────────────────────────────── */
function AdminShell({ screen, dark, compStatus }) {
  const t = getTokens(dark);
  const [nav, setNav] = useState(screen || 'competitions');

  const screenMap = {
    competitions: {
      title: '대회 관리',
      breadcrumb: null,
      actions: <Btn variant="primary" dark={dark} icon="Plus">새 대회 만들기</Btn>,
      component: <CompetitionListScreen dark={dark} compStatus={compStatus} />,
    },
    edit: {
      title: '새 대회 만들기',
      breadcrumb: '← 대회 목록 / 새 대회 만들기',
      actions: null,
      component: <CompetitionEditScreen dark={dark} />,
    },
    detail: {
      title: '대회 상세',
      breadcrumb: '← 대회 목록',
      actions: null,
      component: <CompetitionDetailScreen dark={dark} compStatus={compStatus} />,
    },
    registrations: {
      title: '신청 관리',
      breadcrumb: '대회 관리 / 2026 Seoul CrossFit Open',
      actions: null,
      component: <RegistrationManageScreen dark={dark} />,
    },
    scores: {
      title: '기록 판독',
      breadcrumb: '대회 관리 / 2026 Seoul CrossFit Open',
      actions: null,
      component: <ScoreJudgeScreen dark={dark} />,
    },
    leaderboard: {
      title: '리더보드 프로젝션',
      breadcrumb: '대회 관리 / 2026 Seoul CrossFit Open',
      actions: null,
      component: <LeaderboardScreen dark={dark} />,
    },
  };

  const active = screenMap[nav] || screenMap.competitions;

  return (
    <div style={{ display: 'flex', width: '100%', height: '100%', background: t.bg, fontFamily: 'Geist Variable, system-ui, sans-serif', position: 'relative', overflow: 'hidden' }}>
      <AdminSidebar active={nav} onNav={setNav} dark={dark} />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        <AdminHeader title={active.title} breadcrumb={active.breadcrumb} actions={active.actions} dark={dark} />
        <main style={{ flex: 1, overflowY: 'auto', background: t.bg }}>
          {active.component}
        </main>
      </div>
    </div>
  );
}

Object.assign(window, { AdminShell, getTokens, COMP_STATUS });
