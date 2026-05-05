export function DashboardPage() {
  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">대시보드</h1>
          <p className="text-muted-foreground mt-1">
            Way to Fit 관리 시스템에 오신 것을 환영합니다.
          </p>
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {[
          { title: '총 회원 수', value: '-', description: '활성 회원' },
          { title: '오늘 수업', value: '-', description: '예정된 수업' },
          { title: '이번 달 매출', value: '-', description: '전월 대비' },
          { title: '대기 예약', value: '-', description: '확인 대기 중' },
        ].map((card) => (
          <div
            key={card.title}
            className="rounded-xl border border-border bg-card p-6 shadow-sm"
          >
            <p className="text-sm font-medium text-muted-foreground">
              {card.title}
            </p>
            <p className="mt-2 text-3xl font-bold text-card-foreground">
              {card.value}
            </p>
            <p className="mt-1 text-xs text-muted-foreground">
              {card.description}
            </p>
          </div>
        ))}
      </div>
    </div>
  );
}
