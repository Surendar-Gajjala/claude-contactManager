import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import AppLayout from './components/layout/AppLayout';
import PersonsPage from './pages/persons/PersonsPage';
import PersonFormPage from './pages/persons/PersonFormPage';
import ContactsPage from './pages/contacts/ContactsPage';
import ContactFormPage from './pages/contacts/ContactFormPage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<AppLayout />}>
          <Route index element={<Navigate to="/persons" replace />} />
          <Route path="persons" element={<PersonsPage />} />
          <Route path="persons/new" element={<PersonFormPage />} />
          <Route path="persons/:id/edit" element={<PersonFormPage />} />
          <Route path="contacts" element={<ContactsPage />} />
          <Route path="contacts/new" element={<ContactFormPage />} />
          <Route path="contacts/:id/edit" element={<ContactFormPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
