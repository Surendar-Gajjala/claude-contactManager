import { useCallback, useEffect, useState } from 'react';
import { getErrorMessage } from '../api/axiosClient';
import type { PageResponse } from '../api/types';

export interface FetchPageParams {
  page: number;
  size: number;
  sort?: string;
  search?: string;
}

interface UsePaginatedListOptions<T> {
  size?: number;
  fetcher: (params: FetchPageParams) => Promise<PageResponse<T>>;
}

/** Shared page/search/sort/loading/error state for the Persons and Contacts list pages. */
export function usePaginatedList<T>({ size = 6, fetcher }: UsePaginatedListOptions<T>) {
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [sort, setSort] = useState<string | undefined>(undefined);
  const [data, setData] = useState<PageResponse<T> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reloadToken, setReloadToken] = useState(0);

  const reload = useCallback(() => setReloadToken((token) => token + 1), []);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);

    fetcher({ page, size, sort, search: search || undefined })
      .then((result) => {
        if (!cancelled) setData(result);
      })
      .catch((err: unknown) => {
        if (!cancelled) setError(getErrorMessage(err));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, size, sort, search, reloadToken]);

  function updateSearch(value: string) {
    setSearch(value);
    setPage(0);
  }

  function toggleSort(field: string) {
    setSort((current) => {
      if (!current || !current.startsWith(field)) return `${field},asc`;
      return current.endsWith(',asc') ? `${field},desc` : `${field},asc`;
    });
    setPage(0);
  }

  return { page, setPage, search, setSearch: updateSearch, sort, toggleSort, data, loading, error, reload };
}
