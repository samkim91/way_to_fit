import { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { cn } from '@/lib/utils';
import {
  LayoutDashboard,
  Dumbbell,
  Trophy,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';

interface NavigationItem {
  name: string;
  href: string;
  icon: React.ElementType;
  exact?: boolean;
}

interface NavigationSection {
  items: NavigationItem[];
  dividerBefore?: boolean;
}

const navigationSections: NavigationSection[] = [
  {
    items: [
      { name: '대시보드', href: '/', icon: LayoutDashboard },
    ],
  },
  {
    dividerBefore: true,
    items: [{ name: '대회 관리', href: '/competitions', icon: Trophy }],
  },
];

export function Sidebar() {
  const location = useLocation();
  const [isCollapsed, setIsCollapsed] = useState(false);

  return (
    <aside
      className={cn(
        'flex flex-col border-r border-sidebar-border bg-sidebar transition-all duration-300',
        isCollapsed ? 'w-20' : 'w-64',
      )}
    >
      {/* Logo */}
      <div
        className={cn(
          'flex h-16 items-center border-b border-sidebar-border px-6',
          isCollapsed ? 'justify-center px-0' : 'gap-2',
        )}
      >
        <Dumbbell className="h-6 w-6 shrink-0 text-primary" />
        {!isCollapsed && (
          <span className="truncate text-lg font-bold tracking-tight text-sidebar-foreground">
            Way to Fit
          </span>
        )}
      </div>

      {/* Navigation */}
      <nav className="flex-1 space-y-1 px-3 py-4">
        {/* Toggle Button */}
        <button
          onClick={() => setIsCollapsed(!isCollapsed)}
          className={cn(
            'mb-4 flex w-full items-center rounded-md px-3 py-2 text-sm font-medium transition-colors',
            isCollapsed ? 'justify-center' : 'justify-between',
            'text-sidebar-foreground/70 hover:bg-sidebar-accent/50 hover:text-sidebar-foreground'
          )}
          title={isCollapsed ? '사이드바 펼치기' : undefined}
        >
          {isCollapsed ? (
            <ChevronRight className="h-5 w-5 shrink-0" />
          ) : (
            <>
              <span className="truncate"></span>
              <ChevronLeft className="h-5 w-5 shrink-0" />
            </>
          )}
        </button>

        {navigationSections.map((section, sectionIdx) => (
          <div key={sectionIdx}>
            {section.dividerBefore && (
              <div className="my-2 h-px bg-sidebar-border" />
            )}
            {section.items.map((item) => {
              const isActive =
                item.href === '/'
                  ? location.pathname === '/'
                  : item.exact
                    ? location.pathname === item.href
                    : location.pathname.startsWith(item.href);

              return (
                <Link
                  key={item.name}
                  to={item.href}
                  className={cn(
                    'flex items-center rounded-md px-3 py-2 text-sm font-medium transition-colors',
                    isCollapsed ? 'justify-center' : 'gap-3',
                    isActive
                      ? 'bg-sidebar-accent text-sidebar-accent-foreground'
                      : 'text-sidebar-foreground/70 hover:bg-sidebar-accent/50 hover:text-sidebar-foreground',
                  )}
                  title={isCollapsed ? item.name : undefined}
                >
                  <item.icon className="h-4 w-4 shrink-0" />
                  {!isCollapsed && <span className="truncate">{item.name}</span>}
                </Link>
              );
            })}
          </div>
        ))}
      </nav>
    </aside>
  );
}
