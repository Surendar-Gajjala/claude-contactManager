import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { deleteContact, getContacts } from '../../api/contactApi';
import type { ContactResponse } from '../../api/types';
import { getErrorMessage } from '../../api/axiosClient';
import { usePaginatedList } from '../../hooks/usePaginatedList';
import Pagination from '../../components/common/Pagination';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import Banner from '../../components/common/Banner';
import { PencilIcon, PlusIcon, TrashIcon } from '../../components/common/icons';

const PAGE_SIZE = 6;

interface ContactsNavState {
  success?: string;
}

export default function ContactsPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { page, setPage, search, setSearch, sort, toggleSort, data, loading, error, reload } =
    usePaginatedList<ContactResponse>({ size: PAGE_SIZE, fetcher: getContacts });

  const [searchInput, setSearchInput] = useState('');
  const [deleteTarget, setDeleteTarget] = useState<ContactResponse | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  useEffect(() => {
    const timeout = setTimeout(() => setSearch(searchInput), 300);
    return () => clearTimeout(timeout);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchInput]);

  useEffect(() => {
    const state = location.state as ContactsNavState | null;
    if (state?.success) setSuccessMessage(state.success);
    if (state?.success) navigate(location.pathname, { replace: true, state: null });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function handleConfirmDelete() {
    if (!deleteTarget) return;
    setDeleting(true);
    setDeleteError(null);
    try {
      await deleteContact(deleteTarget.id);
      setDeleteTarget(null);
      setSuccessMessage(`Contact for ${deleteTarget.personName} was deleted.`);
      reload();
    } catch (err) {
      setDeleteError(getErrorMessage(err));
    } finally {
      setDeleting(false);
    }
  }

  const contacts = data?.content ?? [];

  function sortIndicator(field: string) {
    if (!sort?.startsWith(field)) return '';
    return sort.endsWith(',asc') ? '↑' : '↓';
  }

  return (
    <div>
      <div className="mb-6 flex flex-wrap items-center justify-between gap-4">
        <h1 className="text-2xl font-semibold text-gray-900">Contacts</h1>
        <Link
          to="/contacts/new"
          className="inline-flex items-center gap-2 rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500"
        >
          <PlusIcon className="h-4 w-4" />
          Add Contact
        </Link>
      </div>

      {successMessage && <Banner variant="success" message={successMessage} onDismiss={() => setSuccessMessage(null)} />}
      {error && <Banner variant="error" message={error} />}
      {deleteError && <Banner variant="error" message={deleteError} onDismiss={() => setDeleteError(null)} />}

      <div className="mb-4">
        <input
          type="search"
          value={searchInput}
          onChange={(event) => setSearchInput(event.target.value)}
          placeholder="Search by person name..."
          className="w-full max-w-sm rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
        />
      </div>

      <div className="overflow-hidden rounded-lg border border-gray-200 bg-white shadow-sm">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th
                  scope="col"
                  onClick={() => toggleSort('personName')}
                  className="cursor-pointer select-none px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-500 hover:text-gray-700"
                >
                  Person Name {sortIndicator('personName')}
                </th>
                <th
                  scope="col"
                  onClick={() => toggleSort('phoneNumber')}
                  className="cursor-pointer select-none px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-500 hover:text-gray-700"
                >
                  Phone Number {sortIndicator('phoneNumber')}
                </th>
                <th
                  scope="col"
                  onClick={() => toggleSort('contactType')}
                  className="cursor-pointer select-none px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-500 hover:text-gray-700"
                >
                  Contact Type {sortIndicator('contactType')}
                </th>
                <th scope="col" className="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-gray-500">
                  Action
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {loading && (
                <tr>
                  <td colSpan={4} className="px-4 py-8 text-center text-sm text-gray-500">
                    Loading contacts...
                  </td>
                </tr>
              )}
              {!loading && contacts.length === 0 && (
                <tr>
                  <td colSpan={4} className="px-4 py-8 text-center text-sm text-gray-500">
                    {search ? 'No contacts match your search.' : 'No contacts yet. Click "Add Contact" to create one.'}
                  </td>
                </tr>
              )}
              {!loading &&
                contacts.map((contact) => (
                  <tr key={contact.id}>
                    <td className="px-4 py-3 text-sm text-gray-900">{contact.personName}</td>
                    <td className="px-4 py-3 text-sm text-gray-600">{contact.phoneNumber}</td>
                    <td className="px-4 py-3 text-sm text-gray-600">{contact.contactType}</td>
                    <td className="px-4 py-3 text-right text-sm">
                      <div className="flex justify-end gap-3">
                        <button
                          type="button"
                          title="Edit"
                          onClick={() => navigate(`/contacts/${contact.id}/edit`)}
                          className="text-gray-500 hover:text-indigo-600"
                        >
                          <PencilIcon className="h-4 w-4" />
                        </button>
                        <button
                          type="button"
                          title="Delete"
                          onClick={() => setDeleteTarget(contact)}
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
            visibleCount={contacts.length}
            first={data.first}
            last={data.last}
            onPageChange={setPage}
          />
        )}
      </div>

      <ConfirmDialog
        open={deleteTarget !== null}
        title="Delete contact"
        message={deleteTarget ? `Delete this ${deleteTarget.contactType.toLowerCase()} contact for ${deleteTarget.personName}? This cannot be undone.` : ''}
        isLoading={deleting}
        onConfirm={handleConfirmDelete}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  );
}
