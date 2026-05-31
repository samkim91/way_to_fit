import React from 'react';
import type { ColumnDef } from '@tanstack/react-table';
import {
  flexRender,
  getCoreRowModel,
  useReactTable,
} from '@tanstack/react-table';
import { ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from 'lucide-react';

export interface DataTablePagination {
  pageIndex: number;
  pageSize: number;
  totalPages: number;
  totalElements: number;
}

export interface DataTableProps<TData, TValue> {
  columns: ColumnDef<TData, TValue>[];
  data: TData[];
  isLoading?: boolean;
  emptyMessage?: string;
  pagination?: DataTablePagination;
  onPaginationChange?: (pageIndex: number, pageSize: number) => void;
  onRowClick?: (row: TData) => void;
}

export function DataTable<TData, TValue>({
  columns,
  data,
  isLoading,
  emptyMessage = '데이터가 없습니다.',
  pagination,
  onPaginationChange,
  onRowClick,
}: DataTableProps<TData, TValue>) {
  // TanStack Table is intentionally used here as the stateful table engine.
  // eslint-disable-next-line react-hooks/incompatible-library
  const table = useReactTable({
    data,
    columns,
    getCoreRowModel: getCoreRowModel(),
    manualPagination: true,
    pageCount: pagination?.totalPages ?? -1,
  });

  const handlePageChange = (newPageIndex: number) => {
    if (onPaginationChange && pagination) {
      onPaginationChange(newPageIndex, pagination.pageSize);
    }
  };

  const handleSizeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    if (onPaginationChange) {
      onPaginationChange(0, Number(e.target.value));
    }
  };

  const renderPagination = () => {
    if (!pagination) return null;
    const { pageIndex, pageSize, totalPages, totalElements } = pagination;
    
    const current = pageIndex + 1;
    let start = Math.max(1, current - 4);
    let end = start + 9;
    if (end > totalPages) {
      end = totalPages;
      start = Math.max(1, end - 9);
    }
    const pageNumbers = [];
    for (let i = start; i <= end; i++) {
      pageNumbers.push(i);
    }

    return (
      <div className="flex flex-col sm:flex-row items-center justify-between px-6 py-4 bg-muted/10 gap-4 mt-auto">
        <div className="flex flex-col sm:flex-row items-center gap-4 text-sm text-muted-foreground w-full sm:w-auto">
          <span className="font-medium whitespace-nowrap">총 <strong className="text-foreground">{totalElements}</strong>건</span>
          <div className="flex items-center gap-2">
            <label htmlFor="page-size-select" className="sr-only">목록 개수</label>
            <select
              id="page-size-select"
              value={pageSize}
              onChange={handleSizeChange}
              className="h-8 max-w-fit bg-background text-foreground border border-input rounded-md text-xs px-2 focus:ring-1 focus:ring-primary focus:outline-none transition-colors cursor-pointer"
            >
              {[10, 20, 50, 100].map(size => (
                <option key={size} value={size}>{size}개씩 보기</option>
              ))}
            </select>
          </div>
        </div>

        {totalPages > 0 && (
          <div className="flex items-center gap-1.5">
            <button
              onClick={() => handlePageChange(0)}
              disabled={pageIndex === 0 || isLoading}
              className="flex items-center justify-center w-8 h-8 rounded-md text-muted-foreground hover:bg-muted hover:text-foreground disabled:opacity-50 disabled:pointer-events-none transition-colors"
              aria-label="맨 앞 페이지로"
            >
              <ChevronsLeft className="h-4 w-4" />
            </button>
            <button
              onClick={() => handlePageChange(pageIndex - 1)}
              disabled={pageIndex === 0 || isLoading}
              className="flex items-center justify-center w-8 h-8 rounded-md text-muted-foreground hover:bg-muted hover:text-foreground disabled:opacity-50 disabled:pointer-events-none transition-colors"
              aria-label="이전 페이지로"
            >
              <ChevronLeft className="h-4 w-4" />
            </button>
            
            <div className="flex gap-1 mx-1">
              {pageNumbers.map(pageNum => (
                <button
                  key={pageNum}
                  onClick={() => handlePageChange(pageNum - 1)}
                  disabled={isLoading}
                  className={`w-8 h-8 text-sm font-medium rounded-md transition-all ${
                    pageIndex + 1 === pageNum
                      ? 'bg-primary text-primary-foreground shadow-sm scale-110'
                      : 'text-muted-foreground hover:bg-muted hover:text-foreground hover:scale-105'
                  }`}
                >
                  {pageNum}
                </button>
              ))}
            </div>

            <button
              onClick={() => handlePageChange(pageIndex + 1)}
              disabled={pageIndex >= totalPages - 1 || isLoading}
              className="flex items-center justify-center w-8 h-8 rounded-md text-muted-foreground hover:bg-muted hover:text-foreground disabled:opacity-50 disabled:pointer-events-none transition-colors"
              aria-label="다음 페이지로"
            >
              <ChevronRight className="h-4 w-4" />
            </button>
            <button
              onClick={() => handlePageChange(totalPages - 1)}
              disabled={pageIndex >= totalPages - 1 || isLoading}
              className="flex items-center justify-center w-8 h-8 rounded-md text-muted-foreground hover:bg-muted hover:text-foreground disabled:opacity-50 disabled:pointer-events-none transition-colors"
              aria-label="맨 뒤 페이지로"
            >
              <ChevronsRight className="h-4 w-4" />
            </button>
          </div>
        )}
      </div>
    );
  };

  return (
    <div className="bg-card rounded-xl shadow-sm border border-border flex min-h-0 flex-col overflow-hidden">
      <div className="min-h-0 flex-1 overflow-auto w-full">
        <table className="w-full text-sm text-left">
          <thead className="text-xs text-muted-foreground uppercase bg-muted/40 border-b border-border">
            {table.getHeaderGroups().map((headerGroup) => (
              <tr key={headerGroup.id}>
                {headerGroup.headers.map((header) => (
                  <th key={header.id} className="px-6 py-3.5 font-semibold tracking-wider whitespace-nowrap">
                    {header.isPlaceholder
                      ? null
                      : flexRender(
                          header.column.columnDef.header,
                          header.getContext()
                        )}
                  </th>
                ))}
              </tr>
            ))}
          </thead>
          <tbody className="divide-y divide-border">
            {isLoading ? (
              <tr>
                <td colSpan={columns.length} className="px-6 py-12 text-center text-muted-foreground">
                  <div className="flex flex-col justify-center items-center gap-3">
                    <span className="w-6 h-6 border-2 border-primary border-t-transparent rounded-full animate-spin"></span>
                    <span className="text-sm font-medium">데이터를 불러오는 중입니다...</span>
                  </div>
                </td>
              </tr>
            ) : table.getRowModel().rows.length === 0 ? (
              <tr>
                <td colSpan={columns.length} className="px-6 py-16 text-center text-muted-foreground text-sm font-medium">
                  {emptyMessage}
                </td>
              </tr>
            ) : (
              table.getRowModel().rows.map((row) => (
                <tr 
                  key={row.id} 
                  className={`transition-colors ${onRowClick ? 'cursor-pointer hover:bg-muted/50' : 'hover:bg-muted/30'}`}
                  onClick={() => onRowClick && onRowClick(row.original)}
                >
                  {row.getVisibleCells().map((cell) => {
                    const isRightAligned = cell.column.id === 'actions'; // action column exception
                    return (
                      <td key={cell.id} className={`px-6 py-4 ${isRightAligned ? 'text-right' : ''}`}>
                        {flexRender(cell.column.columnDef.cell, cell.getContext())}
                      </td>
                    );
                  })}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
      {renderPagination()}
    </div>
  );
}
