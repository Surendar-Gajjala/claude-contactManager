import { NavLink, Outlet } from 'react-router-dom';
import { AppLogoIcon, ContactIcon, UsersIcon } from '../common/icons';

const NAV_ITEMS = [
  { to: '/persons', label: 'Persons', icon: UsersIcon },
  { to: '/contacts', label: 'Contacts', icon: ContactIcon },
];

export default function AppLayout() {
  return (
    <div className="flex min-h-screen bg-gray-50">
      <aside className="w-56 shrink-0 border-r border-gray-200 bg-white">
        <div className="flex items-center gap-2 px-4 py-5">
          <AppLogoIcon className="h-6 w-6 text-indigo-600" />
          <h1 className="text-lg font-semibold text-gray-900">ContactManagerApp</h1>
        </div>
        <nav className="mt-2 flex flex-col gap-1 px-2">
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium ${
                  isActive ? 'bg-indigo-50 text-indigo-700' : 'text-gray-700 hover:bg-gray-100'
                }`
              }
            >
              <item.icon className="h-4 w-4" />
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>
      <main className="min-w-0 flex-1 overflow-x-hidden px-6 py-6">
        <Outlet />
      </main>
    </div>
  );
}
