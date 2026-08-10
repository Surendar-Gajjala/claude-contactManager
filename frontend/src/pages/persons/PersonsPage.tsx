import { useEffect, useRef, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { deletePerson, getPersons, uploadExcel } from '../../api/personApi';
import type { PersonResponse } from '../../api/types';
import { getErrorMessage } from '../../api/axiosClient';
import { usePaginatedList } from '../../hooks/usePaginatedList';
import Pagination from '../../components/common/Pagination';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import Banner from '../../components/common/Banner';
import { PencilIcon, PlusIcon, TrashIcon, UploadIcon } from '../../components/common/icons';

const PAGE_SIZE = 6;
const ACCEPTED_EXCEL_TYPES = ['.xlsx', '.xls'];

function SortableHeader({
  label,
  field,
  sort,
  onSort,
}: {
  label: string;
  field: string;
  sort?: string;
  onSort: (field: string) => void;
}) {
  const isActive = sort?.startsWith(field);
  const direction = isActive ? (sort?.endsWith(',asc') ? '↑' : '↓') : '';
  return (
    <th
      scope="col"
      className="cursor-pointer select-none px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-500 hover:text-gray-700"
      onClick={() => onSort(field)}
    >
      {label} {direction}
    </th>
  );
}

interface PersonsNavState {
  success?: string;
  warning?: string;
}

export default function PersonsPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { page, setPage, search, setSearch, sort, toggleSort, data, loading, error, reload } =
    usePaginatedList<PersonResponse>({ size: PAGE_SIZE, fetcher: getPersons });

  const [searchInput, setSearchInput] = useState('');
  const [deleteTarget, setDeleteTarget] = useState<PersonResponse | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [warningMessage, setWarningMessage] = useState<string | null>(null);
  const [uploading, setUploading] = useState(false);
  const [uploadMessage, setUploadMessage] = useState<{ variant: 'success' | 'error'; text: string } | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    const timeout = setTimeout(() => setSearch(searchInput), 300);
    return () => clearTimeout(timeout);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchInput]);

  useEffect(() => {
    const state = location.state as PersonsNavState | null;
    if (state?.success) setSuccessMessage(state.success);
    if (state?.warning) setWarningMessage(state.warning);
    if (state?.success || state?.warning) {
      navigate(location.pathname, { replace: true, state: null });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function handleConfirmDelete() {
    if (!deleteTarget) return;
    setDeleting(true);
    setDeleteError(null);
    try {
      await deletePerson(deleteTarget.id);
      setDeleteTarget(null);
      setSuccessMessage(`${deleteTarget.firstName} ${deleteTarget.lastName} was deleted.`);
      reload();
    } catch (err) {
      setDeleteError(getErrorMessage(err));
    } finally {
      setDeleting(false);
    }
  }

  function handleUploadClick() {
    fileInputRef.current?.click();
  }

  async function handleFileSelected(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file) return;

    const lowerName = file.name.toLowerCase();
    if (!ACCEPTED_EXCEL_TYPES.some((ext) => lowerName.endsWith(ext))) {
      setUploadMessage({ variant: 'error', text: 'Please select an Excel file (.xlsx or .xls).' });
      return;
    }

    setUploading(true);
    setUploadMessage(null);
    try {
      const result = await uploadExcel(file);
      setUploadMessage({
        variant: 'success',
        text: `Import complete: ${result.personsCreated} person(s) and ${result.contactsCreated} contact(s) created.`,
      });
      reload();
    } catch (err) {
      setUploadMessage({ variant: 'error', text: getErrorMessage(err) });
    } finally {
      setUploading(false);
    }
  }

  const persons = data?.content ?? [];

  return (
    <div>
      <div className="mb-6 flex flex-wrap items-center justify-between gap-4">
        <h1 className="text-2xl font-semibold text-gray-900">Persons</h1>
        <div className="flex items-center gap-3">
          <input
            ref={fileInputRef}
            type="file"
            accept=".xlsx,.xls"
            className="hidden"
            onChange={handleFileSelected}
          />
          <button
            type="button"
            onClick={handleUploadClick}
            disabled={uploading}
            title="Upload"
            className="inline-flex items-center gap-2 rounded-md border border-gray-300 bg-white px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50"
          >
            <UploadIcon className="h-4 w-4" />
            {uploading ? 'Uploading...' : 'Upload'}
          </button>
          <Link
            to="/persons/new"
            className="inline-flex items-center gap-2 rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500"
          >
            <PlusIcon className="h-4 w-4" />
            Add Person
          </Link>
        </div>
      </div>

      {uploadMessage && (
        <Banner variant={uploadMessage.variant} message={uploadMessage.text} onDismiss={() => setUploadMessage(null)} />
      )}
      {successMessage && <Banner variant="success" message={successMessage} onDismiss={() => setSuccessMessage(null)} />}
      {warningMessage && <Banner variant="error" message={warningMessage} onDismiss={() => setWarningMessage(null)} />}
      {error && <Banner variant="error" message={error} />}
      {deleteError && <Banner variant="error" message={deleteError} onDismiss={() => setDeleteError(null)} />}

      <div className="mb-4">
        <input
          type="search"
          value={searchInput}
          onChange={(event) => setSearchInput(event.target.value)}
          placeholder="Search by first name..."
          className="w-full max-w-sm rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
        />
      </div>

      <div className="overflow-hidden rounded-lg border border-gray-200 bg-white shadow-sm">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <SortableHeader label="First Name" field="firstName" sort={sort} onSort={toggleSort} />
                <SortableHeader label="Last Name" field="lastName" sort={sort} onSort={toggleSort} />
                <SortableHeader label="Email" field="email" sort={sort} onSort={toggleSort} />
                <SortableHeader label="Gender" field="gender" sort={sort} onSort={toggleSort} />
                <th scope="col" className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-500">
                  Address
                </th>
                <th scope="col" className="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-gray-500">
                  Action
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {loading && (
                <tr>
                  <td colSpan={6} className="px-4 py-8 text-center text-sm text-gray-500">
                    Loading persons...
                  </td>
                </tr>
              )}
              {!loading && persons.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-4 py-8 text-center text-sm text-gray-500">
                    {search ? 'No persons match your search.' : 'No persons yet. Click "Add Person" to create one.'}
                  </td>
                </tr>
              )}
              {!loading &&
                persons.map((person) => (
                  <tr key={person.id}>
                    <td className="px-4 py-3 text-sm text-gray-900">{person.firstName}</td>
                    <td className="px-4 py-3 text-sm text-gray-900">{person.lastName}</td>
                    <td className="px-4 py-3 text-sm text-gray-600">{person.email}</td>
                    <td className="px-4 py-3 text-sm text-gray-600">{person.gender}</td>
                    <td className="px-4 py-3 text-sm text-gray-600">{person.address ?? '—'}</td>
                    <td className="px-4 py-3 text-right text-sm">
                      <div className="flex justify-end gap-3">
                        <button
                          type="button"
                          title="Edit"
                          onClick={() => navigate(`/persons/${person.id}/edit`)}
                          className="text-gray-500 hover:text-indigo-600"
                        >
                          <PencilIcon className="h-4 w-4" />
                        </button>
                        <button
                          type="button"
                          title="Delete"
                          onClick={() => setDeleteTarget(person)}
                          className="text-gray-500 hover:text-red-600"
                        >
                          <TrashIcon className="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
            </tbody>
          </table>
        </div>

        {data && (
          <Pagination
            page={page}
            totalElements={data.totalElements}
            visibleCount={persons.length}
            first={data.first}
            last={data.last}
            onPageChange={setPage}
          />
        )}
      </div>

      <ConfirmDialog
        open={deleteTarget !== null}
        title="Delete person"
        message={
          deleteTarget
            ? `Delete ${deleteTarget.firstName} ${deleteTarget.lastName}? This will also delete all of their contacts. This cannot be undone.`
            : ''
        }
        isLoading={deleting}
        onConfirm={handleConfirmDelete}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  );
}
