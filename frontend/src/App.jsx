import React, { useEffect, useState } from 'react';
import { Routes, Route, Link, useNavigate } from 'react-router-dom';
import api, { setToken } from './api.js';

function useAuth() {
  const [user, setUser] = useState(() => JSON.parse(localStorage.getItem('user') || 'null'));
  return { user, setUser };
}

function Login({ setUser }) {
  const [email, setEmail] = useState(''); const [password, setPassword] = useState('');
  const nav = useNavigate();
  const submit = async (e) => {
    e.preventDefault();
    const { data } = await api.post('/auth/login', { email, password });
    setToken(data.data.token); localStorage.setItem('user', JSON.stringify(data.data));
    setUser(data.data);
    nav(data.data.role === 'ADMIN' ? '/admin' : data.data.role === 'COMPANY' ? '/company' : '/jobs');
  };
  return (<form onSubmit={submit}><h2>Login</h2>
    <input placeholder="email" value={email} onChange={(e) => setEmail(e.target.value)} />
    <input placeholder="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
    <button type="submit">Login</button>
    <p>No account? <Link to="/register">Register</Link></p></form>);
}

function Register() {
  const [name, setName] = useState(''); const [email, setEmail] = useState(''); const [password, setPassword] = useState('');
  const [role, setRole] = useState('STUDENT'); const nav = useNavigate();
  const submit = async (e) => {
    e.preventDefault();
    await api.post('/auth/register', { name, email, password, role });
    nav('/login');
  };
  return (<form onSubmit={submit}><h2>Register</h2>
    <input placeholder="full name" value={name} onChange={(e) => setName(e.target.value)} />
    <input placeholder="email" value={email} onChange={(e) => setEmail(e.target.value)} />
    <input placeholder="password (8+ chars, upper/lower/digit/special)" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
    <select value={role} onChange={(e) => setRole(e.target.value)}><option>STUDENT</option><option>COMPANY</option></select>
    <button type="submit">Register</button></form>);
}

function Jobs() {
  const [jobs, setJobs] = useState([]); const [q, setQ] = useState('');
  const load = async () => { const { data } = await api.get('/jobs', { params: q ? { keyword: q } : {} }); setJobs(data.data.content ?? data.data); };
  useEffect(() => { load(); }, []);
  const apply = async (id) => { await api.post('/applications', { jobId: id }); alert('Applied'); };
  return (<div><h2>Open Jobs</h2>
    <input placeholder="search title/skill/location" value={q} onChange={(e) => setQ(e.target.value)} />
    <button onClick={load}>Search</button>
    {jobs.map((j) => (<div key={j.id} style={{ border: '1px solid #ccc', margin: 8, padding: 8 }}>
      <b>{j.title}</b> @ {j.companyName} — {j.location} [{j.jobType}] match={j.matchScore}
      <p>{j.description}</p>
      <button onClick={() => apply(j.id)}>Apply</button></div>))}</div>);
}

function Company() {
  const [jobs, setJobs] = useState([]); const [title, setTitle] = useState('SDE Intern');
  const load = async () => { const { data } = await api.get('/jobs'); setJobs(data.data); };
  useEffect(() => { load(); }, []);
  const post = async () => {
    await api.post('/jobs', { title, description: 'Great role', location: 'Bengaluru', jobType: 'INTERNSHIP', salary: 60000, minimumCgpa: 7.0, requiredSkills: 'Java,Spring', deadline: '2026-12-31T23:59:59Z' });
    load();
  };
  return (<div><h2>Company Dashboard</h2><button onClick={post}>Post: {title}</button>
    {jobs.map((j) => (<div key={j.id}>{j.title} — {j.status}</div>))}</div>);
}

function Admin() {
  const [stats, setStats] = useState(null);
  useEffect(() => { api.get('/admin/stats').then((r) => setStats(r.data.data)).catch(() => setStats({ note: 'admin only' })); }, []);
  return (<div><h2>Admin</h2><pre>{JSON.stringify(stats, null, 2)}</pre></div>);
}

export default function App() {
  const { user, setUser } = useAuth();
  const logout = () => { setToken(null); localStorage.removeItem('user'); setUser(null); };
  return (<div style={{ padding: 16, fontFamily: 'sans-serif' }}>
    <nav><Link to="/">Home</Link> | <Link to="/jobs">Jobs</Link> | <Link to="/company">Company</Link> | <Link to="/admin">Admin</Link> | {user ? <button onClick={logout}>Logout {user.email}</button> : <Link to="/login">Login</Link>}</nav>
    <h1>Campus Hiring Platform</h1>
    <Routes>
      <Route path="/login" element={<Login setUser={setUser} />} />
      <Route path="/register" element={<Register />} />
      <Route path="/jobs" element={<Jobs />} />
      <Route path="/company" element={<Company />} />
      <Route path="/admin" element={<Admin />} />
      <Route path="/" element={<p>Internship &amp; campus hiring: students apply, companies post, admins moderate.</p>} />
    </Routes></div>);
}
