interface PaginationProps {
  page: number;
  totalElements: number;
  visibleCount: number;
  first: boolean;
  last: boolean;
  onPageChange: (page: number) => void;
}

export default function Pagination({
  page,
  totalElements,
  visibleCount,
  first,
  last,
  onPageChange,
}: PaginationProps) {
  return (
    <div className="flex items-center justify-between border-t border-gray-200 px-4 py-3">
      <p className="text-sm text-gray-600">
        Showing {visibleCount} of {totalElements}
      </p>
      <div className="flex gap-2">
        <button
          type="button"
          onClick={() => onPageChange(page - 1)}
          disabled={first}
          className="rounded border border-gray-300 px-3 py-1 text-sm text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
        >
          Previous
        </button>
        <button
          type="button"
          onClick={() => onPageChange(page + 1)}
          disabled={last}
          className="rounded border border-gray-300 px-3 py-1 text-sm text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
        >
          Next
        </button>
      </div>
    </div>
  );
}
