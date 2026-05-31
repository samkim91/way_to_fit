
// Way To Fit — App Competition Screens (Flutter-style Mobile UI)
// Shared to window

const { useState: useStateApp, useEffect: useEffectApp, useRef: useRefApp } = React;

/* ─── Icon ──────────────────────────────────────────────────── */
function AppIcon({ name, size = 20, color = 'currentColor', strokeWidth = 1.5 }) {
  const ref = useRefApp(null);
  useEffectApp(() => {
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

/* ─── App theme tokens ──────────────────────────────────────── */
function getAppTokens(dark) {
  return {
    bg:          dark ? '#0F172A' : '#F8F9FA',
    card:        dark ? '#1E293B' : '#FFFFFF',
    surface:     dark ? '#1E293B' : '#FFFFFF',
    surfaceAlt:  dark ? '#263044' : '#F1F5F9',
    border:      dark ? '#2D3748' : '#F1F5F9',
    borderStrong:dark ? '#374151' : '#E2E8F0',
    fg:          dark ? '#F1F5F9' : '#1A202C',
    fgMuted:     dark ? '#94A3B8' : '#718096',
    fgSubtle:    dark ? '#475569' : '#94A3B8',
    primary:     '#2563EB',
    primaryText: dark ? '#93C5FD' : '#2563EB',
    topBar:      dark ? '#111827' : '#FFFFFF',
    bottomNav:   dark ? '#111827' : '#FFFFFF',
    statusBar:   dark ? '#111827' : '#FFFFFF',
    heroGrad:    dark ? 'linear-gradient(160deg,#1e293b 0%,#0f172a 100%)' : 'linear-gradient(160deg,#1e3a8a 0%,#1e40af 100%)',
  };
}

/* ─── App status chips ──────────────────────────────────────── */
const APP_STATUS = {
  REGISTRATION_OPEN:   { label: '신청중',   bg: '#DBEAFE', color: '#1E40AF' },
  REGISTRATION_CLOSED: { label: '신청마감', bg: '#FED7D7', color: '#9B2C2C' },
  IN_PROGRESS:         { label: '진행중',   bg: '#FED7AA', color: '#9A3412' },
  COMPLETED:           { label: '종료',     bg: '#E2E8F0', color: '#475569' },
  COMING_SOON:         { label: '예정',     bg: '#F3E8FF', color: '#7C3AED' },
  DRAFT:               { label: 'DRAFT',    bg: '#E2E8F0', color: '#475569' },
};

/* ─── Shared sub-components ─────────────────────────────────── */
function AppChip({ cfg, style: extra }) {
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: 3,
      background: cfg.bg, color: cfg.color,
      padding: '3px 9px', borderRadius: 9999,
      fontSize: 11, fontWeight: 600, whiteSpace: 'nowrap', ...extra,
    }}>{cfg.label}</span>
  );
}

function AppStatusBar({ dark }) {
  const t = getAppTokens(dark);
  return (
    <div style={{ height: 20, background: t.statusBar, display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 14px', flexShrink: 0 }}>
      <span style={{ fontSize: 10, fontWeight: 700, color: t.fg, fontFamily: 'Geist Mono Variable, monospace' }}>9:41</span>
      <div style={{ display: 'flex', gap: 3, alignItems: 'center' }}>
        {[3, 4, 5].map(h => <div key={h} style={{ width: 3, height: h, background: t.fg, borderRadius: 1 }} />)}
        <AppIcon name="Wifi" size={10} color={t.fg} />
        <AppIcon name="Battery" size={10} color={t.fg} />
      </div>
    </div>
  );
}

function AppTopBar({ title, showBack, dark, actions }) {
  const t = getAppTokens(dark);
  return (
    <div style={{ height: 52, background: t.topBar, borderBottom: `1px solid ${t.border}`, display: 'flex', alignItems: 'center', padding: '0 4px', gap: 2, flexShrink: 0 }}>
      {showBack ? (
        <div style={{ width: 44, height: 44, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <AppIcon name="ArrowLeft" size={20} color={t.fg} />
        </div>
      ) : (
        <div style={{ width: 44, height: 44, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <div style={{ width: 26, height: 26, background: '#2563EB', borderRadius: 7, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <AppIcon name="Dumbbell" size={13} color="#fff" />
          </div>
        </div>
      )}
      <span style={{ flex: 1, fontSize: 15, fontWeight: 600, color: t.fg }}>{title}</span>
      {(actions || []).map((a, i) => (
        <div key={i} style={{ width: 44, height: 44, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <AppIcon name={a} size={20} color={t.fgMuted} />
        </div>
      ))}
    </div>
  );
}

const APP_TABS = [
  { id: 'home',        icon: 'Home',        label: '홈' },
  { id: 'wod',         icon: 'Dumbbell',    label: 'WOD' },
  { id: 'competition', icon: 'Trophy',      label: '대회' },
  { id: 'records',     icon: 'BarChart2',   label: '기록' },
  { id: 'profile',     icon: 'User',        label: '마이' },
];

function AppBottomNav({ active, dark }) {
  const t = getAppTokens(dark);
  return (
    <div style={{ height: 60, background: t.bottomNav, borderTop: `1px solid ${t.border}`, display: 'flex', flexShrink: 0 }}>
      {APP_TABS.map(tab => {
        const isActive = tab.id === active;
        return (
          <div key={tab.id} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 2, cursor: 'pointer' }}>
            <div style={{ width: 48, height: 28, borderRadius: 14, background: isActive ? (t.primary + '22') : 'transparent', display: 'flex', alignItems: 'center', justifyContent: 'center', transition: 'background 150ms' }}>
              <AppIcon name={tab.icon} size={18} color={isActive ? t.primary : t.fgMuted} strokeWidth={isActive ? 2 : 1.5} />
            </div>
            <span style={{ fontSize: 9, color: isActive ? t.primary : t.fgMuted, fontWeight: isActive ? 600 : 400 }}>{tab.label}</span>
          </div>
        );
      })}
    </div>
  );
}

/* ─── App data ──────────────────────────────────────────────── */
const APP_COMPS = [
  {
    id: 1,
    name: '2026 Seoul CrossFit Open',
    dateRange: '2026.05.01 — 2026.05.10',
    status: 'REGISTRATION_OPEN',
    dday: 'D-2',
    participants: 47,
    regDeadline: '신청 마감: 2026.04.30',
    fee: 50000,
    bank: '국민은행 123-456-789012',
    banker: '박주최',
    stages: [
      {
        name: '예선', type: 'ONLINE', dateRange: '04.10 — 04.20',
        events: [
          { id: 1, name: 'Event 1 — 21.1', type: 'INDIVIDUAL', gender: 'MEN+WOMEN', scale: 'RXD / SCALED', wod: 'For Time', cap: null, deadline: '04.20 23:59', myScore: '8:30', myStatus: 'APPROVED' },
          { id: 2, name: 'Event 2 — 21.2', type: 'TEAM',       gender: 'MIXED',     scale: 'RXD / SCALED', wod: 'AMRAP 20분', cap: 20, deadline: '04.20 23:59', myScore: null, myStatus: null },
          { id: 3, name: 'Event 3 — 21.3', type: 'INDIVIDUAL', gender: 'MEN+WOMEN', scale: 'RXD / SCALED', wod: 'For Time', cap: 15, deadline: '04.20 23:59', myScore: null, myStatus: null },
        ],
      },
    ],
    myReg: { type: '개인전', scale: 'RXD', status: 'CONFIRMED' },
  },
  {
    id: 2,
    name: '2026 Summer Throwdown',
    dateRange: '2026.07.15 — 2026.07.16',
    status: 'COMING_SOON',
    dday: 'D-75',
    participants: 12,
    regDeadline: '신청 예정: 2026.05.01 오픈',
    fee: 60000,
    bank: '',
    banker: '',
    stages: [],
    myReg: null,
  },
  {
    id: 3,
    name: '2025 Winter CrossFit Challenge',
    dateRange: '2025.12.20 — 2025.12.21',
    status: 'COMPLETED',
    dday: '종료',
    participants: 134,
    regDeadline: '신청 마감됨',
    fee: 50000,
    bank: '',
    banker: '',
    stages: [],
    myReg: null,
  },
  {
    id: 4,
    name: '2026 Spring Box War',
    dateRange: '2026.03.22 — 2026.03.22',
    status: 'IN_PROGRESS',
    dday: '진행중',
    participants: 88,
    regDeadline: '신청 마감됨',
    fee: 40000,
    bank: '',
    banker: '',
    stages: [],
    myReg: null,
  },
];

const APP_SCORE_STATUS = {
  SUBMITTED:    { label: '제출됨',  bg: '#F1F5F9', color: '#64748B' },
  UNDER_REVIEW: { label: '검토 중', bg: '#DBEAFE', color: '#1E40AF' },
  APPROVED:     { label: '승인',    bg: '#DCFCE7', color: '#166534' },
  ADJUSTED:     { label: '조정됨',  bg: '#FEF3C7', color: '#92400E' },
  REJECTED:     { label: '거절됨',  bg: '#FEE2E2', color: '#991B1B' },
};

const APP_LB = [
  { rank: 1, name: '홍길동', box: 'CF Seoul',   total: '4pt',  ev1: 1, ev2: 2, ev3: 1, me: false },
  { rank: 2, name: '김철수', box: 'CF Busan',   total: '6pt',  ev1: 2, ev2: 1, ev3: 3, me: false },
  { rank: 3, name: '나 (박민준)', box: 'WTF Seoul', total: '8pt', ev1: 3, ev2: 3, ev3: 2, me: true },
  { rank: 4, name: '최지원', box: 'CF Incheon', total: '8pt',  ev1: 4, ev2: 4, ev3: null, me: false },
  { rank: 5, name: '이수빈', box: 'WTF Seoul',  total: '14pt', ev1: 5, ev2: 5, ev3: 4, me: false },
  { rank: 6, name: '강동원', box: 'CF Mapo',    total: '17pt', ev1: 6, ev2: 6, ev3: 5, me: false },
];

/* ══════════════════════════════════════════════════════════════
   APP SCREEN 1 — 대회 목록
══════════════════════════════════════════════════════════════ */
function AppCompetitionListScreen({ dark, compStatus }) {
  const t = getAppTokens(dark);
  const [filter, setFilter] = useStateApp('신청중');
  const filters = ['신청중', '진행중', '예정', '종료'];

  const comps = APP_COMPS.map(c => ({ ...c, status: c.id === 1 ? compStatus : c.status }));

  return (
    <div style={{ flex: 1, overflowY: 'auto', paddingBottom: 8 }}>
      {/* Filter chips */}
      <div style={{ display: 'flex', gap: 6, padding: '10px 14px', borderBottom: `1px solid ${t.border}`, overflowX: 'auto' }}>
        {filters.map(f => (
          <button key={f} onClick={() => setFilter(f)} style={{
            padding: '5px 14px', borderRadius: 9999,
            border: `1px solid ${filter === f ? t.primary : t.borderStrong}`,
            background: filter === f ? t.primary : 'transparent',
            color: filter === f ? '#fff' : t.fgMuted,
            fontSize: 12, fontWeight: filter === f ? 600 : 400,
            cursor: 'pointer', fontFamily: 'inherit', whiteSpace: 'nowrap',
          }}>{f}</button>
        ))}
      </div>

      <div style={{ padding: '10px 14px', display: 'flex', flexDirection: 'column', gap: 10 }}>
        {comps.map(comp => {
          const s = APP_STATUS[comp.status] || APP_STATUS.DRAFT;
          return (
            <div key={comp.id} style={{ background: t.card, borderRadius: 14, overflow: 'hidden', border: `1px solid ${t.border}`, boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
              {/* Banner placeholder */}
              <div style={{ height: 90, background: t.heroGrad, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <AppIcon name="Trophy" size={28} color="rgba(255,255,255,0.25)" />
              </div>
              <div style={{ padding: '12px 14px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 4 }}>
                  <div style={{ fontSize: 13, fontWeight: 700, color: t.fg, flex: 1, paddingRight: 8 }}>{comp.name}</div>
                  <AppChip cfg={s} />
                </div>
                <div style={{ fontSize: 11, color: t.fgMuted, marginBottom: 4, display: 'flex', alignItems: 'center', gap: 4 }}>
                  <AppIcon name="Calendar" size={11} color={t.fgSubtle} />
                  {comp.dateRange}
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ fontSize: 11, color: t.fgMuted }}>{comp.regDeadline}</span>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                    <AppIcon name="Users" size={11} color={t.fgSubtle} />
                    <span style={{ fontSize: 11, fontWeight: 600, color: t.fg, fontFamily: 'Geist Mono Variable, monospace' }}>{comp.participants}명</span>
                  </div>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

/* ══════════════════════════════════════════════════════════════
   APP SCREEN 2 — 대회 상세
══════════════════════════════════════════════════════════════ */
function AppCompetitionDetailScreen({ dark, compStatus }) {
  const t = getAppTokens(dark);
  const comp = { ...APP_COMPS[0], status: compStatus };
  const s = APP_STATUS[comp.status] || APP_STATUS.DRAFT;
  const scoreStatusMap = APP_SCORE_STATUS;

  return (
    <div style={{ flex: 1, overflowY: 'auto', paddingBottom: 80 }}>
      {/* Hero */}
      <div style={{ height: 100, background: t.heroGrad, display: 'flex', flexDirection: 'column', justifyContent: 'flex-end', padding: '0 16px 14px', flexShrink: 0 }}>
        <div style={{ fontSize: 18, fontWeight: 700, color: '#fff', lineHeight: 1.2, marginBottom: 4 }}>{comp.name}</div>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
          <AppChip cfg={s} />
          <span style={{ fontSize: 11, color: 'rgba(255,255,255,0.7)', display: 'flex', alignItems: 'center', gap: 3 }}>
            <AppIcon name="Calendar" size={11} color="rgba(255,255,255,0.5)" />{comp.dateRange}
          </span>
        </div>
      </div>

      <div style={{ padding: '14px 16px', display: 'flex', flexDirection: 'column', gap: 14 }}>
        {/* 참가비 */}
        <div style={{ background: t.surfaceAlt, borderRadius: 12, padding: '12px 14px' }}>
          <div style={{ fontSize: 11, fontWeight: 600, color: t.fgMuted, textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 6 }}>참가비</div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ fontSize: 17, fontWeight: 700, color: t.fg, fontFamily: 'Geist Mono Variable, monospace' }}>{comp.fee.toLocaleString()}원</span>
            <span style={{ fontSize: 11, color: t.fgMuted }}>{comp.bank}&nbsp;({comp.banker})</span>
          </div>
        </div>

        {/* Stage & Events */}
        {comp.stages.map(stage => (
          <div key={stage.name}>
            <div style={{ fontSize: 12, fontWeight: 600, color: t.fgMuted, textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 8, display: 'flex', alignItems: 'center', gap: 6 }}>
              {stage.name} ({stage.type}) · {stage.dateRange}
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {stage.events.map(ev => {
                const sc = ev.myStatus ? APP_SCORE_STATUS[ev.myStatus] : null;
                return (
                  <div key={ev.id} style={{ background: t.card, border: `1px solid ${t.border}`, borderRadius: 12, padding: '12px 14px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 4 }}>
                      <div style={{ fontSize: 13, fontWeight: 600, color: t.fg }}>{ev.name}</div>
                      <span style={{ background: ev.type === 'TEAM' ? '#DBEAFE' : t.surfaceAlt, color: ev.type === 'TEAM' ? '#1E40AF' : t.fgMuted, padding: '2px 7px', borderRadius: 9999, fontSize: 10, fontWeight: 500 }}>{ev.type === 'TEAM' ? '팀전' : '개인전'}</span>
                    </div>
                    <div style={{ fontSize: 11, color: t.fgMuted, marginBottom: 4 }}>{ev.gender} · {ev.scale} · {ev.wod}{ev.cap ? ` ${ev.cap}분` : ''}</div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <span style={{ fontSize: 10, color: t.fgSubtle }}>마감: {ev.deadline}</span>
                      {sc ? (
                        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                          <span style={{ fontSize: 13, fontWeight: 700, color: t.fg, fontFamily: 'Geist Mono Variable, monospace' }}>나의 기록: {ev.myScore}</span>
                          <span style={{ background: sc.bg, color: sc.color, padding: '2px 7px', borderRadius: 9999, fontSize: 10, fontWeight: 600 }}>{sc.label}</span>
                        </div>
                      ) : (
                        <button style={{ fontSize: 11, color: t.primary, background: 'none', border: `1px solid ${t.primary}`, borderRadius: 8, padding: '3px 9px', cursor: 'pointer', fontFamily: 'inherit', display: 'flex', alignItems: 'center', gap: 4 }}>
                          기록 제출 <AppIcon name="ChevronRight" size={11} color={t.primary} />
                        </button>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        ))}

        {/* Leaderboard button */}
        <button style={{ width: '100%', padding: '12px', background: t.surfaceAlt, border: `1px solid ${t.border}`, borderRadius: 12, fontSize: 13, fontWeight: 600, color: t.primary, cursor: 'pointer', fontFamily: 'inherit', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6 }}>
          <AppIcon name="BarChart2" size={16} color={t.primary} />
          리더보드 보기
        </button>

        {/* My registration status */}
        {comp.myReg && (
          <div style={{ background: '#EFF6FF', border: '1px solid #DBEAFE', borderRadius: 12, padding: '12px 14px' }}>
            <div style={{ fontSize: 11, fontWeight: 600, color: '#1E40AF', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 6 }}>나의 참가 상태</div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
              <span style={{ fontSize: 12, color: '#1E40AF' }}>{comp.myReg.type} · {comp.myReg.scale}</span>
              <span style={{ background: comp.myReg.status === 'CONFIRMED' ? '#DCFCE7' : '#FEF3C7', color: comp.myReg.status === 'CONFIRMED' ? '#166534' : '#92400E', padding: '2px 9px', borderRadius: 9999, fontSize: 11, fontWeight: 700 }}>
                {comp.myReg.status === 'CONFIRMED' ? 'CONFIRMED ✓' : 'PENDING ⏳'}
              </span>
            </div>
            <div style={{ display: 'flex', gap: 8 }}>
              <button style={{ flex: 1, padding: '9px', background: '#2563EB', color: '#fff', border: 'none', borderRadius: 9, fontSize: 12, fontWeight: 600, cursor: 'pointer', fontFamily: 'inherit' }}>기록 제출하기</button>
              <button style={{ flex: 1, padding: '9px', background: 'transparent', color: '#2563EB', border: '1px solid #DBEAFE', borderRadius: 9, fontSize: 12, cursor: 'pointer', fontFamily: 'inherit' }}>신청 내역 보기</button>
            </div>
          </div>
        )}
      </div>

      {/* CTA — 미신청 */}
      {!comp.myReg && (
        <div style={{ padding: '0 16px 20px' }}>
          <button style={{ width: '100%', padding: '14px', background: '#2563EB', color: '#fff', border: 'none', borderRadius: 14, fontSize: 15, fontWeight: 700, cursor: 'pointer', fontFamily: 'inherit', boxShadow: '0 4px 16px rgba(37,99,235,0.3)' }}>
            대회 신청하기
          </button>
        </div>
      )}
    </div>
  );
}

/* ══════════════════════════════════════════════════════════════
   APP SCREEN 3 — 참가 신청 (개인)
══════════════════════════════════════════════════════════════ */
function AppRegistrationScreen({ dark }) {
  const t = getAppTokens(dark);
  const [regType, setRegType] = useStateApp('개인전');
  const [gender, setGender] = useStateApp('남성');
  const [scale, setScale] = useStateApp('RXD');

  return (
    <div style={{ flex: 1, overflowY: 'auto', paddingBottom: 20 }}>
      <div style={{ padding: '16px 16px', display: 'flex', flexDirection: 'column', gap: 16 }}>
        {/* 참가 구분 */}
        <div>
          <div style={{ fontSize: 11, fontWeight: 600, color: t.fgMuted, marginBottom: 8, textTransform: 'uppercase', letterSpacing: '0.05em' }}>참가 구분</div>
          <div style={{ display: 'flex', gap: 8 }}>
            {['개인전', '팀전'].map(v => (
              <button key={v} onClick={() => setRegType(v)} style={{
                flex: 1, padding: '10px', borderRadius: 12, cursor: 'pointer', fontFamily: 'inherit', fontSize: 13, fontWeight: 600,
                border: regType === v ? `2px solid ${t.primary}` : `1px solid ${t.borderStrong}`,
                background: regType === v ? (t.primary + '18') : t.card,
                color: regType === v ? t.primary : t.fgMuted,
              }}>{v}</button>
            ))}
          </div>
        </div>

        <div style={{ height: 1, background: t.border }} />

        {/* 성별 */}
        {regType === '개인전' && (
          <div>
            <div style={{ fontSize: 11, fontWeight: 600, color: t.fgMuted, marginBottom: 8, textTransform: 'uppercase', letterSpacing: '0.05em' }}>성별 *</div>
            <div style={{ display: 'flex', gap: 8 }}>
              {['남성', '여성'].map(v => (
                <button key={v} onClick={() => setGender(v)} style={{
                  flex: 1, padding: '10px', borderRadius: 12, cursor: 'pointer', fontFamily: 'inherit', fontSize: 13, fontWeight: 600,
                  border: gender === v ? `2px solid ${t.primary}` : `1px solid ${t.borderStrong}`,
                  background: gender === v ? (t.primary + '18') : t.card,
                  color: gender === v ? t.primary : t.fgMuted,
                }}>{v}</button>
              ))}
            </div>
          </div>
        )}

        {/* 스케일 */}
        <div>
          <div style={{ fontSize: 11, fontWeight: 600, color: t.fgMuted, marginBottom: 8, textTransform: 'uppercase', letterSpacing: '0.05em' }}>스케일 카테고리 *</div>
          <div style={{ display: 'flex', gap: 8 }}>
            {['RXD', 'SCALED'].map(v => (
              <button key={v} onClick={() => setScale(v)} style={{
                flex: 1, padding: '10px', borderRadius: 12, cursor: 'pointer', fontFamily: 'inherit', fontSize: 13, fontWeight: 600,
                border: scale === v ? `2px solid ${t.primary}` : `1px solid ${t.borderStrong}`,
                background: scale === v ? (t.primary + '18') : t.card,
                color: scale === v ? t.primary : t.fgMuted,
              }}>{v}</button>
            ))}
          </div>
        </div>

        <div style={{ height: 1, background: t.border }} />

        {/* 참가비 안내 */}
        <div style={{ background: t.surfaceAlt, borderRadius: 12, padding: '14px' }}>
          <div style={{ fontSize: 11, fontWeight: 600, color: t.fgMuted, marginBottom: 8, textTransform: 'uppercase', letterSpacing: '0.05em' }}>참가비 안내</div>
          <div style={{ fontSize: 15, fontWeight: 700, color: t.fg, marginBottom: 4, fontFamily: 'Geist Mono Variable, monospace' }}>50,000원</div>
          <div style={{ fontSize: 12, color: t.fgMuted, marginBottom: 2 }}>국민은행 123-456-789012 (박주최)</div>
          <div style={{ fontSize: 11, color: t.fgSubtle }}>입금 후 주최자가 확인하면 승인됩니다.</div>
        </div>

        {/* 입금자명 */}
        <div>
          <div style={{ fontSize: 11, fontWeight: 600, color: t.fgMuted, marginBottom: 8, textTransform: 'uppercase', letterSpacing: '0.05em' }}>입금자명 / 메모 (선택)</div>
          <div style={{ background: t.card, border: `1px solid ${t.borderStrong}`, borderRadius: 12, padding: '11px 14px', fontSize: 13, color: t.fg }}>
            박민준 0501
          </div>
        </div>

        {/* CTA */}
        <button style={{ width: '100%', padding: '14px', background: '#2563EB', color: '#fff', border: 'none', borderRadius: 14, fontSize: 15, fontWeight: 700, cursor: 'pointer', fontFamily: 'inherit', boxShadow: '0 4px 16px rgba(37,99,235,0.3)' }}>
          신청하기
        </button>
      </div>
    </div>
  );
}

/* ══════════════════════════════════════════════════════════════
   APP SCREEN 4 — 기록 제출
══════════════════════════════════════════════════════════════ */
function AppScoreSubmitScreen({ dark }) {
  const t = getAppTokens(dark);
  const [scale, setScale] = useStateApp('RXD');
  const [dnf, setDnf] = useStateApp(false);
  const [min, setMin] = useStateApp('08');
  const [sec, setSec] = useStateApp('30');

  return (
    <div style={{ flex: 1, overflowY: 'auto', paddingBottom: 20 }}>
      {/* WOD header */}
      <div style={{ background: t.heroGrad, padding: '14px 16px 16px' }}>
        <div style={{ fontSize: 12, color: 'rgba(255,255,255,0.6)', marginBottom: 2, textTransform: 'uppercase', letterSpacing: '0.05em' }}>For Time · 개인전 · {scale}</div>
        <div style={{ fontSize: 15, fontWeight: 700, color: '#fff', marginBottom: 2 }}>Event 1 — 21.1</div>
        <div style={{ fontSize: 11, color: 'rgba(255,255,255,0.6)', display: 'flex', alignItems: 'center', gap: 4 }}>
          <AppIcon name="Clock" size={11} color="rgba(255,255,255,0.5)" />
          제출 마감: 2026.04.20 23:59
        </div>
      </div>

      {/* WOD desc */}
      <div style={{ margin: '12px 16px', background: t.surfaceAlt, borderRadius: 12, padding: '12px 14px' }}>
        <div style={{ fontSize: 11, fontWeight: 600, color: t.fgMuted, marginBottom: 6, textTransform: 'uppercase', letterSpacing: '0.05em' }}>WOD 설명</div>
        <div style={{ fontFamily: 'Geist Mono Variable, monospace', fontSize: 13, color: t.fg, lineHeight: 1.7 }}>
          21-15-9<br/>
          Thrusters (43/29kg)<br/>
          Pull-ups
        </div>
      </div>

      <div style={{ padding: '0 16px', display: 'flex', flexDirection: 'column', gap: 14 }}>
        {/* 스케일 */}
        <div>
          <div style={{ fontSize: 11, fontWeight: 600, color: t.fgMuted, marginBottom: 8, textTransform: 'uppercase', letterSpacing: '0.05em' }}>스케일 카테고리</div>
          <div style={{ display: 'flex', gap: 8 }}>
            {['RXD', 'SCALED'].map(v => (
              <button key={v} onClick={() => setScale(v)} style={{
                flex: 1, padding: '9px', borderRadius: 12, cursor: 'pointer', fontFamily: 'inherit', fontSize: 13, fontWeight: 600,
                border: scale === v ? `2px solid ${t.primary}` : `1px solid ${t.borderStrong}`,
                background: scale === v ? (t.primary + '18') : t.card,
                color: scale === v ? t.primary : t.fgMuted,
              }}>{v}</button>
            ))}
          </div>
        </div>

        {/* 기록 입력 */}
        <div>
          <div style={{ fontSize: 11, fontWeight: 600, color: t.fgMuted, marginBottom: 8, textTransform: 'uppercase', letterSpacing: '0.05em' }}>기록 * (For Time)</div>
          <div style={{ display: 'flex', gap: 8, alignItems: 'center', opacity: dnf ? 0.4 : 1 }}>
            <div style={{ flex: 1, background: t.card, border: `1px solid ${t.borderStrong}`, borderRadius: 12, padding: '14px', textAlign: 'center' }}>
              <div style={{ fontSize: 28, fontWeight: 700, color: t.fg, fontFamily: 'Geist Mono Variable, monospace' }}>{min}</div>
              <div style={{ fontSize: 10, color: t.fgSubtle, marginTop: 2 }}>분</div>
            </div>
            <span style={{ fontSize: 22, color: t.fgMuted, fontWeight: 700 }}>:</span>
            <div style={{ flex: 1, background: t.card, border: `1px solid ${t.borderStrong}`, borderRadius: 12, padding: '14px', textAlign: 'center' }}>
              <div style={{ fontSize: 28, fontWeight: 700, color: t.fg, fontFamily: 'Geist Mono Variable, monospace' }}>{sec}</div>
              <div style={{ fontSize: 10, color: t.fgSubtle, marginTop: 2 }}>초</div>
            </div>
          </div>
          {/* DNF */}
          <div onClick={() => setDnf(!dnf)} style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 10, cursor: 'pointer' }}>
            <div style={{ width: 18, height: 18, borderRadius: 4, border: `2px solid ${dnf ? t.primary : t.borderStrong}`, background: dnf ? t.primary : 'transparent', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              {dnf && <AppIcon name="Check" size={12} color="#fff" />}
            </div>
            <span style={{ fontSize: 13, color: t.fg }}>DNF (완수 못함)</span>
          </div>
        </div>

        {/* YouTube URL */}
        <div>
          <div style={{ fontSize: 11, fontWeight: 600, color: t.fgMuted, marginBottom: 6, textTransform: 'uppercase', letterSpacing: '0.05em' }}>YouTube URL *</div>
          <div style={{ background: t.card, border: `1px solid ${t.borderStrong}`, borderRadius: 12, padding: '11px 14px', fontSize: 13, color: t.fgMuted }}>
            https://youtu.be/...
          </div>
          <div style={{ marginTop: 6, fontSize: 11, color: t.fgSubtle, display: 'flex', alignItems: 'center', gap: 4 }}>
            <AppIcon name="Info" size={12} color={t.fgSubtle} />
            영상은 공개 또는 링크 공개로 설정해주세요.
          </div>
        </div>

        <button style={{ width: '100%', padding: '14px', background: '#2563EB', color: '#fff', border: 'none', borderRadius: 14, fontSize: 15, fontWeight: 700, cursor: 'pointer', fontFamily: 'inherit', boxShadow: '0 4px 16px rgba(37,99,235,0.3)' }}>
          제출하기
        </button>
      </div>
    </div>
  );
}

/* ══════════════════════════════════════════════════════════════
   APP SCREEN 5 — 리더보드
══════════════════════════════════════════════════════════════ */
function AppLeaderboardScreen({ dark }) {
  const t = getAppTokens(dark);
  const [lbTab, setLbTab] = useStateApp('종합');
  const [type, setType] = useStateApp('개인전');
  const [gender, setGender] = useStateApp('남');
  const [scale, setScale] = useStateApp('RXD');
  const lbTabs = ['종합', 'Event 1', 'Event 2'];
  const medals = { 1: '🥇', 2: '🥈', 3: '🥉' };

  return (
    <div style={{ flex: 1, overflowY: 'auto', paddingBottom: 8 }}>
      {/* Filter bar */}
      <div style={{ background: t.card, borderBottom: `1px solid ${t.border}`, padding: '10px 14px', display: 'flex', flexDirection: 'column', gap: 8 }}>
        <div style={{ display: 'flex', gap: 6 }}>
          {lbTabs.map(tb => (
            <button key={tb} onClick={() => setLbTab(tb)} style={{
              flex: 1, padding: '6px', borderRadius: 8, border: `1px solid ${lbTab === tb ? t.primary : t.borderStrong}`,
              background: lbTab === tb ? t.primary : 'transparent',
              color: lbTab === tb ? '#fff' : t.fgMuted,
              fontSize: 11, fontWeight: lbTab === tb ? 700 : 400, cursor: 'pointer', fontFamily: 'inherit',
            }}>{tb}</button>
          ))}
        </div>
        <div style={{ display: 'flex', gap: 4, flexWrap: 'wrap' }}>
          {[['타입', ['개인전', '팀전'], type, setType], ['성별', ['남', '여', '전체'], gender, setGender], ['스케일', ['RXD', 'SCALED'], scale, setScale]].map(([label, opts, val, set]) => (
            <div key={label} style={{ display: 'flex', gap: 3, alignItems: 'center' }}>
              <span style={{ fontSize: 10, color: t.fgSubtle, marginRight: 2 }}>{label}:</span>
              {opts.map(v => (
                <button key={v} onClick={() => set(v)} style={{
                  padding: '3px 8px', borderRadius: 6, border: `1px solid ${val === v ? t.primary : t.borderStrong}`,
                  background: val === v ? t.primary : 'transparent',
                  color: val === v ? '#fff' : t.fgMuted,
                  fontSize: 10, cursor: 'pointer', fontFamily: 'inherit',
                }}>{v}</button>
              ))}
            </div>
          ))}
        </div>
      </div>

      {/* Section label */}
      <div style={{ padding: '8px 14px 4px', fontSize: 11, fontWeight: 600, color: t.fgMuted }}>
        종합 순위 — {type} · {gender} · {scale}
      </div>

      {/* Rows */}
      <div style={{ padding: '4px 14px', display: 'flex', flexDirection: 'column', gap: 6 }}>
        {APP_LB.map(row => (
          <div key={row.rank} style={{
            background: row.me ? '#EFF6FF' : t.card,
            border: row.me ? '2px solid #DBEAFE' : `1px solid ${t.border}`,
            borderRadius: 12, padding: '11px 12px',
            boxShadow: row.me ? '0 2px 8px rgba(37,99,235,0.12)' : 'none',
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <span style={{ fontSize: 18, width: 28, textAlign: 'center', flexShrink: 0 }}>
                {medals[row.rank] || <span style={{ fontSize: 14, fontWeight: 700, color: t.fgSubtle, fontFamily: 'Geist Mono Variable, monospace' }}>{row.rank}</span>}
              </span>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 13, fontWeight: row.me ? 700 : 500, color: row.me ? '#1E40AF' : t.fg }}>{row.name}</div>
                <div style={{ fontSize: 10, color: t.fgSubtle, marginTop: 1 }}>
                  {row.box} &nbsp;·&nbsp; Ev.1: {row.ev1} &nbsp;Ev.2: {row.ev2} &nbsp;Ev.3: {row.ev3 ?? '—'}
                </div>
              </div>
              <span style={{ fontSize: 15, fontWeight: 700, color: row.me ? '#2563EB' : t.fg, fontFamily: 'Geist Mono Variable, monospace' }}>{row.total}</span>
            </div>
          </div>
        ))}
      </div>

      {/* My rank sticky note */}
      <div style={{ margin: '10px 14px', background: '#FFF7ED', border: '1px solid #FED7AA', borderRadius: 12, padding: '10px 12px', display: 'flex', alignItems: 'center', gap: 8 }}>
        <AppIcon name="Star" size={14} color="#F97316" />
        <span style={{ fontSize: 12, fontWeight: 600, color: '#9A3412' }}>나 (박민준) — 현재 3위  8pt</span>
      </div>

      <div style={{ padding: '0 14px 4px', fontSize: 10, color: t.fgSubtle, textAlign: 'right' }}>마지막 갱신: 30초 전</div>
    </div>
  );
}

/* ══════════════════════════════════════════════════════════════
   APP SCREEN 6 — 선수 프로필
══════════════════════════════════════════════════════════════ */
function AppAthleteProfileScreen({ dark }) {
  const t = getAppTokens(dark);
  const [open1, setOpen1] = useStateApp(true);
  const [open2, setOpen2] = useStateApp(false);

  const history = [
    {
      id: 1, comp: '2026 Seoul CrossFit Open', type: '개인전', scale: 'RXD', finalRank: '3위',
      events: [
        { name: 'Event 1', score: '8:30', status: 'APPROVED', rank: '2위' },
        { name: 'Event 2', score: '14:20', status: 'APPROVED', rank: '3위' },
        { name: 'Event 3', score: '10:05', status: 'APPROVED', rank: '1위' },
      ],
    },
    {
      id: 2, comp: '2025 Winter CrossFit Challenge', type: '개인전', scale: 'RXD', finalRank: '5위',
      events: [
        { name: 'Event 1', score: '9:12', status: 'APPROVED', rank: '5위' },
        { name: 'Event 2', score: '12:45', status: 'APPROVED', rank: '4위' },
      ],
    },
  ];

  return (
    <div style={{ flex: 1, overflowY: 'auto', paddingBottom: 20 }}>
      {/* Profile hero */}
      <div style={{ padding: '20px 16px 16px', display: 'flex', alignItems: 'center', gap: 14 }}>
        <div style={{ width: 64, height: 64, borderRadius: '50%', background: '#2563EB', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, fontSize: 22, fontWeight: 700, color: '#fff' }}>박</div>
        <div>
          <div style={{ fontSize: 20, fontWeight: 700, color: t.fg }}>박민준</div>
          <div style={{ fontSize: 12, color: t.fgMuted, marginTop: 2, display: 'flex', alignItems: 'center', gap: 4 }}>
            <AppIcon name="MapPin" size={12} color={t.fgSubtle} />
            CrossFit Seoul
          </div>
        </div>
      </div>

      {/* One-liner */}
      <div style={{ margin: '0 16px 14px', background: t.surfaceAlt, borderRadius: 10, padding: '10px 14px' }}>
        <span style={{ fontSize: 13, color: t.fgMuted, fontStyle: 'italic' }}>"매일 조금씩 더 강해진다"</span>
      </div>

      {/* Stats row */}
      <div style={{ display: 'flex', gap: 8, margin: '0 16px 16px' }}>
        {[{ v: '2', label: '대회 참가' }, { v: '6', label: '이벤트 기록' }, { v: '3위', label: '최고 순위' }].map(s => (
          <div key={s.label} style={{ flex: 1, background: t.card, border: `1px solid ${t.border}`, borderRadius: 12, padding: '10px', textAlign: 'center' }}>
            <div style={{ fontSize: 18, fontWeight: 700, color: t.fg, fontFamily: 'Geist Mono Variable, monospace' }}>{s.v}</div>
            <div style={{ fontSize: 10, color: t.fgSubtle, marginTop: 2 }}>{s.label}</div>
          </div>
        ))}
      </div>

      {/* Divider */}
      <div style={{ height: 1, background: t.border, margin: '0 16px 14px' }} />

      {/* Competition history */}
      <div style={{ padding: '0 16px', fontSize: 13, fontWeight: 600, color: t.fg, marginBottom: 8 }}>대회 이력</div>
      <div style={{ padding: '0 16px', display: 'flex', flexDirection: 'column', gap: 8 }}>
        {history.map(h => {
          const isOpen = h.id === 1 ? open1 : open2;
          const setOpen = h.id === 1 ? setOpen1 : setOpen2;
          return (
            <div key={h.id} style={{ background: t.card, border: `1px solid ${t.border}`, borderRadius: 12, overflow: 'hidden' }}>
              <div onClick={() => setOpen(!isOpen)} style={{ padding: '12px 14px', display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer' }}>
                <AppIcon name={isOpen ? 'ChevronDown' : 'ChevronRight'} size={14} color={t.fgMuted} />
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontSize: 13, fontWeight: 600, color: t.fg }}>{h.comp}</div>
                  <div style={{ fontSize: 11, color: t.fgMuted, marginTop: 1 }}>{h.type} · {h.scale} · 최종 {h.finalRank}</div>
                </div>
                <span style={{ fontSize: 13, fontWeight: 700, color: '#2563EB' }}>{h.finalRank}</span>
              </div>
              {isOpen && (
                <div style={{ padding: '0 14px 12px', display: 'flex', flexDirection: 'column', gap: 6 }}>
                  {h.events.map(ev => {
                    const sc = APP_SCORE_STATUS[ev.status];
                    return (
                      <div key={ev.name} style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '7px 10px', background: t.surfaceAlt, borderRadius: 8 }}>
                        <span style={{ fontSize: 11, color: t.fgMuted, width: 60, flexShrink: 0 }}>{ev.name}</span>
                        <span style={{ fontSize: 13, fontWeight: 700, color: t.fg, fontFamily: 'Geist Mono Variable, monospace', flex: 1 }}>{ev.score}</span>
                        <span style={{ background: sc.bg, color: sc.color, padding: '2px 7px', borderRadius: 9999, fontSize: 10, fontWeight: 600 }}>{sc.label}</span>
                        <span style={{ fontSize: 12, fontWeight: 600, color: '#2563EB', width: 28, textAlign: 'right' }}>→ {ev.rank}</span>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

/* ─── Phone Shell ────────────────────────────────────────────── */
function PhoneShell({ screen, dark, compStatus }) {
  const t = getAppTokens(dark);
  const SCREENS = {
    competitionList:   { title: '대회',       tab: 'competition', showBack: false, actions: ['Search'], component: <AppCompetitionListScreen dark={dark} compStatus={compStatus} /> },
    competitionDetail: { title: '대회 상세',  tab: 'competition', showBack: true,  actions: ['Share2'],  component: <AppCompetitionDetailScreen dark={dark} compStatus={compStatus} /> },
    registration:      { title: '대회 신청',  tab: 'competition', showBack: true,  actions: [],          component: <AppRegistrationScreen dark={dark} /> },
    scoreSubmit:       { title: '기록 제출',  tab: 'competition', showBack: true,  actions: [],          component: <AppScoreSubmitScreen dark={dark} /> },
    leaderboard:       { title: '리더보드',   tab: 'competition', showBack: true,  actions: [],          component: <AppLeaderboardScreen dark={dark} /> },
    athleteProfile:    { title: '선수 프로필', tab: 'competition', showBack: true, actions: ['Share2'],  component: <AppAthleteProfileScreen dark={dark} /> },
  };
  const sc = SCREENS[screen] || SCREENS.competitionList;

  return (
    <div style={{
      width: 360, height: 720,
      background: t.bg, borderRadius: 36,
      boxShadow: '0 24px 64px rgba(0,0,0,0.22)',
      overflow: 'hidden', position: 'relative',
      display: 'flex', flexDirection: 'column',
      border: `5px solid ${dark ? '#374151' : '#1A202C'}`,
      fontFamily: 'Geist Variable, system-ui, sans-serif',
    }}>
      <AppStatusBar dark={dark} />
      <AppTopBar title={sc.title} showBack={sc.showBack} dark={dark} actions={sc.actions} />
      {sc.component}
      <AppBottomNav active={sc.tab} dark={dark} />
    </div>
  );
}

Object.assign(window, { PhoneShell });
