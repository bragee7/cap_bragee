import React, { useEffect, useState, createContext, useContext } from 'react';
import { Routes, Route, Link, NavLink, useNavigate, Navigate } from 'react-router-dom';
import api, { setToken } from './api.js';

// ── helpers ──────────────────────────────────────────────────────────
const fmtDate = (iso) => {
  if (!iso) return '—';
  try { return new Date(iso).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' }); } catch { return iso; }
};
const fmtSalary = (v) => v == null ? 'Not disclosed' : `₹ ${Number(v).toLocaleString('en-IN')}/mo`;
const fmtDeadline = (iso) => {
  if (!iso) return 'Open';
  const d = new Date(iso);
  const diff = Math.ceil((d - Date.now()) / 86400000);
  if (diff < 0) return 'Closed';
  if (diff === 0) return 'Last day';
  if (diff === 1) return '1 day left';
  return `${diff} days left`;
};
const matchClass = (s) => s >= 0.7 ? 'high' : s >= 0.4 ? 'mid' : 'low';
const statusBadge = (s) => {
  const m = {
    OPEN:'primary', CLOSED:'danger', APPLIED:'primary', SHORTLISTED:'warning', INTERVIEW:'warning',
    OFFERED:'success', ACCEPTED:'success', REJECTED:'danger', WITHDRAWN:'danger',
    SCHEDULED:'primary', COMPLETED:'success', CANCELLED:'danger', PENDING:'warning',
  };
  return m[s] || 'primary';
};

// ── toast ────────────────────────────────────────────────────────────
const ToastCtx = createContext(null);
function useToast(){
  const ctx = useContext(ToastCtx);
  if(!ctx) throw new Error('toast outside provider');
  return ctx;
}
function ToastProvider({ children }){
  const [toasts, setToasts] = useState([]);
  const push = (msg, kind='ok') => {
    const id = Date.now()+Math.random();
    setToasts(t=>[...t,{id,msg,kind}]);
    setTimeout(()=>setToasts(t=>t.filter(x=>x.id!==id)), 3500);
  };
  return (
    <ToastCtx.Provider value={{ push, ok: (m)=>push(m,'ok'), err:(m)=>push(m,'err') }}>
      {children}
      <div className="toast-wrap">
        {toasts.map(t=>(
          <div key={t.id} className={`toast ${t.kind}`}>{t.msg}</div>
        ))}
      </div>
    </ToastCtx.Provider>
  );
}

// ── auth ─────────────────────────────────────────────────────────────
function useAuthState(){
  const [user, setUser] = useState(()=> {
    try { return JSON.parse(localStorage.getItem('user')||'null'); } catch { return null; }
  });
  const save = (u) => {
    if(u){ localStorage.setItem('user', JSON.stringify(u)); if(u.token) setToken(u.token); }
    else { localStorage.removeItem('user'); setToken(null); }
    setUser(u);
  };
  const logout = () => save(null);
  return { user, setUser: save, logout, isAuthed: !!user };
}

// ── header ───────────────────────────────────────────────────────────
function Header({ user, logout }){
  const [q, setQ] = useState('');
  const nav = useNavigate();
  const goSearch = (e)=>{
    e.preventDefault();
    nav(`/jobs?keyword=${encodeURIComponent(q)}`);
  };
  return (
    <header className="header">
      <div className="container header-inner">
        <Link to="/" className="brand">
          <span className="brand-badge">◈</span>
          CampusHire
          <span style={{color:'var(--muted)',fontWeight:600,fontSize:12,marginLeft:4,letterSpacing:'.04em'}}>INTERNSHIP PLATFORM</span>
        </Link>

        <nav className="nav">
          <NavLink to="/" end className={({isActive})=>isActive?'active':''}>Home</NavLink>
          <NavLink to="/jobs" className={({isActive})=>isActive?'active':''}>Jobs</NavLink>
          {user?.role==='STUDENT' && <NavLink to="/student" className={({isActive})=>isActive?'active':''}>Student</NavLink>}
          {user?.role==='COMPANY' && <NavLink to="/company" className={({isActive})=>isActive?'active':''}>Company</NavLink>}
          {user?.role==='ADMIN' && <NavLink to="/admin" className={({isActive})=>isActive?'active':''}>Admin</NavLink>}
          {user && <NavLink to="/notifications" className={({isActive})=>isActive?'active':''}>Notifications</NavLink>}
        </nav>

        <div style={{display:'flex',gap:10,alignItems:'center'}}>
          <form onSubmit={goSearch} className="search" style={{display:'none'}} >
            <input className="input" placeholder="Search jobs…" value={q} onChange={e=>setQ(e.target.value)} />
          </form>
          {user ? (
            <div className="user-chip">
              <span className="avatar">{(user.name||user.email||'U').slice(0,1).toUpperCase()}</span>
              <span style={{fontWeight:700,fontSize:13,maxWidth:120,overflow:'hidden',textOverflow:'ellipsis',whiteSpace:'nowrap'}}>{user.name||user.email}</span>
              <span className="role-badge">{user.role}</span>
              <button className="icon-btn" title="Logout" onClick={logout} style={{border:'none',background:'var(--surface-2)'}}>↗</button>
            </div>
          ) : (
            <>
              <Link to="/login" className="btn btn-ghost btn-sm">Sign in</Link>
              <Link to="/register" className="btn btn-primary btn-sm">Get started</Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}

// ── Home ─────────────────────────────────────────────────────────────
function Home(){
  const [stats, setStats] = useState(null);
  useEffect(()=>{
    api.get('/jobs', { params:{ size:1 }}).then(r=>{
      const total = r.data?.data?.totalElements ?? r.data?.data?.content?.length ?? '—';
      setStats(total);
    }).catch(()=>{});
  },[]);
  return (
    <>
      <section className="hero">
        <div className="container hero-grid">
          <div>
            <div className="badge primary" style={{marginBottom:12}}>Trusted by 200+ colleges &amp; companies</div>
            <h1>The <span>campus hiring</span> platform that actually ships offers.</h1>
            <p>Students discover internships matched to their skills &amp; CGPA. Companies post, shortlist, interview and roll offers — with state-machine guarantees, audit trails and real matching scores.</p>
            <div className="hero-actions">
              <Link to="/jobs" className="btn btn-primary">Browse open roles →</Link>
              <Link to="/register" className="btn btn-ghost">Create account</Link>
            </div>
            <div className="stat-row">
              <div className="stat"><b>{stats ?? '—'}</b><span>Open opportunities</span></div>
              <div className="stat"><b>10</b><span>Business rules enforced</span></div>
              <div className="stat"><b>3</b><span>Roles · Student · Company · Admin</span></div>
            </div>
          </div>

          <div className="hero-card">
            <div className="hero-card-head">
              <b>Featured workflow</b><span className="badge">Live demo</span>
            </div>
            <div style={{padding:18,display:'grid',gap:14}}>
              <div className="feature"><i>①</i><div><b>Post in 30 seconds</b><div className="muted" style={{fontSize:13}}>Title, skills (comma-separated), CGPA floor, deadline, salary — validated server-side.</div></div></div>
              <div className="feature"><i>②</i><div><b>Smart match score</b><div className="muted" style={{fontSize:13}}>Jaccard on skills + CGPA gate → 0–100 score per student per job.</div></div></div>
              <div className="feature"><i>③</i><div><b>Apply → Interview → Offer</b><div className="muted" style={{fontSize:13}}>No duplicates, no late applies, forward-only status machine, single active offer.</div></div></div>
              <div style={{display:'flex',gap:8,flexWrap:'wrap'}}>
                <span className="chip">Java 17 · Spring Boot 3.2</span><span className="chip">PostgreSQL · Flyway</span><span className="chip">React + Vite</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <div className="container page">
        <div className="grid">
          <div className="card" style={{gridColumn:'span 4'}}>
            <h3>For students</h3>
            <p className="muted">Build profile (college, CGPA, skills), browse with match %, apply once per job, track interviews &amp; offers.</p>
            <Link to="/register" className="btn btn-ghost btn-sm" style={{marginTop:10}}>Register as Student</Link>
          </div>
          <div className="card" style={{gridColumn:'span 4'}}>
            <h3>For companies</h3>
            <p className="muted">Verify once, post jobs, see Applicants, move status forward, schedule interviews, issue offers.</p>
            <Link to="/register" className="btn btn-ghost btn-sm" style={{marginTop:10}}>Register as Company</Link>
          </div>
          <div className="card" style={{gridColumn:'span 4'}}>
            <h3>For admins</h3>
            <p className="muted">Verify companies, monitor stats (users/jobs/apps), audit logs &amp; notifications.</p>
            <Link to="/login" className="btn btn-ghost btn-sm" style={{marginTop:10}}>Admin login</Link>
          </div>
        </div>

        <div className="card" style={{marginTop:16}}>
          <h3>Business rules you can trust</h3>
          <div className="chips" style={{marginTop:10}}>
            <span className="chip">BR-01 no duplicate application</span>
            <span className="chip">BR-02 open jobs only</span>
            <span className="chip">BR-03 deadline enforced</span>
            <span className="chip">BR-04 CGPA gate</span>
            <span className="chip">BR-05 forward-only state machine</span>
            <span className="chip">One active offer per application</span>
            <span className="chip">Future-only interviews</span>
          </div>
          <p className="muted" style={{marginTop:10,fontSize:13}}>Try the Rules: create two students — one below CGPA floor — and watch BR-04 block. Try applying twice — BR-01 blocks. Try moving REJECTED → SHORTLISTED — BR-05 blocks.</p>
        </div>
      </div>
    </>
  );
}

// ── Auth ─────────────────────────────────────────────────────────────
function Login({ setUser }){
  const [email,setEmail]=useState(''); const [password,setPassword]=useState('');
  const [loading,setLoading]=useState(false); const [err,setErr]=useState('');
  const nav = useNavigate(); const toast = useToast();
  const submit=async(e)=>{
    e.preventDefault(); setErr(''); setLoading(true);
    try{
      const { data } = await api.post('/auth/login', { email, password });
      const u = data.data;
      setToken(u.token); localStorage.setItem('user', JSON.stringify(u));
      setUser(u);
      toast.ok(`Welcome back, ${u.name}!`);
      nav(u.role==='ADMIN'?'/admin':u.role==='COMPANY'?'/company':'/jobs');
    }catch(ex){
      const msg = ex.response?.data?.message || ex.response?.data?.error || 'Login failed';
      setErr(msg); toast.err(msg);
    }finally{ setLoading(false); }
  };
  return (
    <div className="container page" style={{maxWidth:520}}>
      <div className="card" style={{padding:24}}>
        <h2 style={{margin:'0 0 6px'}}>Sign in</h2>
        <p className="muted" style={{margin:'0 0 18px'}}>Use your campus or company credentials.</p>
        {err && <div style={{background:'#fee2e2',border:'1px solid #fecaca',padding:10,borderRadius:10,marginBottom:12,fontSize:13,color:'#991b1b'}}>{err}</div>}
        <form onSubmit={submit} className="form">
          <div><label className="label">Email</label><input className="input" placeholder="you@college.edu" value={email} onChange={e=>setEmail(e.target.value)} required /></div>
          <div><label className="label">Password</label><input className="input" type="password" value={password} onChange={e=>setPassword(e.target.value)} required /></div>
          <button className="btn btn-primary" disabled={loading}>{loading?'Signing in…':'Sign in'}</button>
        </form>
        <p className="muted" style={{marginTop:14,fontSize:13}}>No account? <Link to="/register">Create one</Link> · Demo admin: seed via backend or register then promote in DB.</p>
        <div style={{marginTop:14,background:'var(--surface-2)',border:'1px solid var(--border)',borderRadius:12,padding:12}}>
          <b style={{fontSize:12,letterSpacing:'.06em',textTransform:'uppercase',color:'var(--muted)'}}>Quick demo</b>
          <div className="muted" style={{fontSize:13,marginTop:6}}>Register as <b>Student</b> → complete profile → browse Jobs with match score. <br/>Register as <b>Company</b> → wait for Admin to verify (or use seeded company) → post jobs.</div>
        </div>
      </div>
    </div>
  );
}
function Register(){
  const [name,setName]=useState(''); const [email,setEmail]=useState(''); const [password,setPassword]=useState(''); const [role,setRole]=useState('STUDENT');
  const [loading,setLoading]=useState(false); const [err,setErr]=useState(''); const nav=useNavigate(); const toast=useToast();
  const submit=async(e)=>{
    e.preventDefault(); setErr(''); setLoading(true);
    try{
      await api.post('/auth/register', { name, email, password, role });
      toast.ok('Account created — please sign in.');
      nav('/login');
    }catch(ex){
      const msg = ex.response?.data?.message || ex.response?.data?.error || 'Registration failed';
      setErr(msg); toast.err(msg);
    }finally{ setLoading(false); }
  };
  return (
    <div className="container page" style={{maxWidth:560}}>
      <div className="card" style={{padding:24}}>
        <h2 style={{margin:'0 0 6px'}}>Create your account</h2>
        <p className="muted" style={{margin:'0 0 18px'}}> студенты and companies register here. Admins are seeded.</p>
        {err && <div style={{background:'#fee2e2',border:'1px solid #fecaca',padding:10,borderRadius:10,marginBottom:12,fontSize:13,color:'#991b1b'}}>{err}</div>}
        <form onSubmit={submit} className="form">
          <div><label className="label">Full name</label><input className="input" placeholder="Aarav Sharma" value={name} onChange={e=>setName(e.target.value)} required /></div>
          <div className="form-row">
            <div><label className="label">Email</label><input className="input" placeholder="you@college.edu" value={email} onChange={e=>setEmail(e.target.value)} required /></div>
            <div><label className="label">Role</label><select className="select" value={role} onChange={e=>setRole(e.target.value)}><option value="STUDENT">Student</option><option value="COMPANY">Company</option></select></div>
          </div>
          <div><label className="label">Password</label><input className="input" type="password" placeholder="Min 8 chars, upper/lower/digit/special" value={password} onChange={e=>setPassword(e.target.value)} required /></div>
          <button className="btn btn-primary" disabled={loading}>{loading?'Creating…':'Create account'}</button>
        </form>
        <p className="muted" style={{marginTop:14,fontSize:13}}>Already have an account? <Link to="/login">Sign in</Link></p>
      </div>
    </div>
  );
}

// ── Jobs ─────────────────────────────────────────────────────────────
function Jobs({ user }){
  const [jobs,setJobs]=useState([]); const [page,setPage]=useState(0); const [totalPages,setTotalPages]=useState(0); const [total,setTotal]=useState(0);
  const [keyword,setKeyword]=useState(new URLSearchParams(location.search).get('keyword')||'');
  const [locationFilter,setLocationFilter]=useState(''); const [jobType,setJobType]=useState('');
  const [loading,setLoading]=useState(false); const toast=useToast();
  const [selected,setSelected]=useState(null);

  const load=async(p=0)=>{
    setLoading(true);
    try{
      const params={ page:p, size:9 };
      if(keyword) params.keyword=keyword;
      if(locationFilter) params.location=locationFilter;
      if(jobType) params.jobType=jobType;
      const { data } = await api.get('/jobs', { params });
      const pageData = data.data;
      setJobs(pageData.content ?? pageData ?? []);
      setTotalPages(pageData.totalPages ?? 1);
      setTotal(pageData.totalElements ?? (pageData.content?.length ?? 0));
      setPage(pageData.number ?? p);
    }catch(e){ toast.err('Failed to load jobs'); }
    finally{ setLoading(false); }
  };
  useEffect(()=>{ load(0); },[]);
  const apply=async(id)=>{
    if(!user){ toast.err('Please sign in as Student to apply'); return; }
    if(user.role!=='STUDENT'){ toast.err('Only Students can apply'); return; }
    try{ await api.post('/applications', { jobId:id }); toast.ok('Application submitted ✓'); }
    catch(ex){ const msg=ex.response?.data?.message||ex.response?.data?.error||'Apply failed'; toast.err(msg); }
  };

  return (
    <div className="container page">
      <div style={{display:'flex',justifyContent:'space-between',alignItems:'end',gap:16,flexWrap:'wrap'}}>
        <div>
          <h2 className="section-title">Open roles</h2>
          <p className="section-sub">{total} opportunities · sorted by newest · match % personalized for you</p>
        </div>
        {user?.role==='COMPANY' && <Link to="/company" className="btn btn-primary btn-sm">Post a job</Link>}
      </div>

      <div className="card" style={{marginTop:16,padding:14}}>
        <div className="toolbar">
          <div className="search">
            <span style={{position:'absolute',left:12,top:'50%',transform:'translateY(-50%)',color:'var(--muted)'}}>⌕</span>
            <input className="input" style={{paddingLeft:38}} placeholder="Search title, skills, company…" value={keyword} onChange={e=>setKeyword(e.target.value)} onKeyDown={e=>e.key==='Enter'&&(e.preventDefault(),load(0))} />
          </div>
          <input className="input" style={{maxWidth:180}} placeholder="Location (e.g. Bengaluru)" value={locationFilter} onChange={e=>setLocationFilter(e.target.value)} />
          <select className="select" style={{maxWidth:160}} value={jobType} onChange={e=>setJobType(e.target.value)}>
            <option value="">All types</option><option value="INTERNSHIP">Internship</option><option value="FULL_TIME">Full-time</option><option value="PART_TIME">Part-time</option>
          </select>
          <button className="btn btn-primary btn-sm" onClick={()=>load(0)}>Search</button>
          <button className="btn btn-ghost btn-sm" onClick={()=>{setKeyword('');setLocationFilter('');setJobType('');load(0);}}>Reset</button>
        </div>
      </div>

      {loading ? (
        <div className="grid" style={{marginTop:16}}>
          {Array.from({length:6}).map((_,i)=>(<div key={i} className="card" style={{gridColumn:'span 4'}}><div className="skeleton" style={{height:18,width:'60%'}}/><div className="skeleton" style={{height:12,marginTop:10}}/><div className="skeleton" style={{height:12,marginTop:8,width:'80%'}}/></div>))}
        </div>
      ) : jobs.length===0 ? (
        <div className="card empty" style={{marginTop:16}}><b>No roles found</b>Try adjusting keyword, location or type.</div>
      ) : (
        <>
          <div className="grid" style={{marginTop:16}}>
            {jobs.map(j=>(
              <div key={j.id} className="card" style={{gridColumn:'span 4',display:'flex',flexDirection:'column'}}>
                <div className="job-meta">
                  <span className="badge primary">{j.jobType?.replace('_',' ')}</span>
                  <span className="badge">{j.status}</span>
                  <span className="muted" style={{fontSize:12}}>⏰ {fmtDeadline(j.deadline)}</span>
                  {typeof j.matchScore==='number' && (
                    <span className="match"><span className={`match-dot ${matchClass(j.matchScore)}`}>{Math.round(j.matchScore*100)}%</span><span style={{fontSize:11,color:'var(--muted)',fontWeight:700}}>MATCH</span></span>
                  )}
                </div>
                <div className="job-title">{j.title}</div>
                <div className="company">{j.companyName} · {j.location || 'Remote'}</div>
                <p className="muted" style={{fontSize:13,margin:'8px 0',display:'-webkit-box',WebkitLineClamp:3,WebkitBoxOrient:'vertical',overflow:'hidden',minHeight:58}}>{j.description}</p>
                <div className="chips">
                  {(j.requiredSkills||'').split(',').filter(Boolean).slice(0,4).map(s=><span key={s} className="chip">{s.trim()}</span>)}
                  {j.minimumCgpa!=null && <span className="chip">CGPA ≥ {j.minimumCgpa}</span>}
                  <span className="chip">{fmtSalary(j.salary)}</span>
                </div>
                <div style={{display:'flex',gap:8,marginTop:14}}>
                  <button className="btn btn-primary btn-sm" style={{flex:1}} onClick={()=>apply(j.id)}>Apply</button>
                  <button className="btn btn-ghost btn-sm" onClick={()=>setSelected(j)}>Details</button>
                </div>
              </div>
            ))}
          </div>
          <div style={{display:'flex',gap:8,justifyContent:'center',alignItems:'center',marginTop:18}}>
            <button className="btn btn-ghost btn-sm" disabled={page<=0} onClick={()=>load(page-1)}>← Prev</button>
            <span className="muted" style={{fontSize:13}}>Page {page+1} / {totalPages || 1} · {total} total</span>
            <button className="btn btn-ghost btn-sm" disabled={page+1>=totalPages} onClick={()=>load(page+1)}>Next →</button>
          </div>
        </>
      )}

      {selected && (
        <div className="overlay" onClick={()=>setSelected(null)}>
          <div className="modal" onClick={e=>e.stopPropagation()}>
            <div className="modal-head"><b>{selected.title}</b><button className="icon-btn" onClick={()=>setSelected(null)}>✕</button></div>
            <div className="modal-body" style={{display:'grid',gap:10}}>
              <div className="muted" style={{fontSize:13}}>{selected.companyName} · {selected.location} · {selected.jobType}</div>
              <p>{selected.description}</p>
              <div className="chips">
                {(selected.requiredSkills||'').split(',').map(s=> s.trim() && <span key={s} className="chip">{s.trim()}</span>)}
              </div>
              <div style={{display:'grid',gridTemplateColumns:'1fr 1fr',gap:10,fontSize:13}}>
                <div><b>Salary</b><div className="muted">{fmtSalary(selected.salary)}</div></div>
                <div><b>CGPA floor</b><div className="muted">{selected.minimumCgpa ?? '—'}</div></div>
                <div><b>Deadline</b><div className="muted">{fmtDate(selected.deadline)}</div></div>
                <div><b>Status</b><div><span className={`badge ${statusBadge(selected.status)}`}>{selected.status}</span></div></div>
              </div>
              <button className="btn btn-primary" onClick={()=>{setSelected(null); apply(selected.id);}}>Apply to this role</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

// ── Student ──────────────────────────────────────────────────────────
function StudentPage({ user }){
  const toast=useToast();
  const [tab,setTab]=useState('profile');
  const [profile,setProfile]=useState(null); const [apps,setApps]=useState([]); const [interviews,setInterviews]=useState([]); const [offers,setOffers]=useState([]);
  const [form,setForm]=useState({ college:'', department:'', year:'', cgpa:'', skills:'', resumeUrl:'' });

  const loadAll=async()=>{
    try{
      const [p,a,i,o] = await Promise.all([
        api.get('/students/me').catch(()=>null),
        api.get('/applications/mine').catch(()=>({data:{data:[]}})),
        api.get('/interviews/mine').catch(()=>({data:{data:[]}})),
        api.get('/offers/mine').catch(()=>({data:{data:[]}})),
      ]);
      if(p?.data?.data){ setProfile(p.data.data); setForm({ college:p.data.data.college||'', department:p.data.data.department||'', year:p.data.data.year||'', cgpa:p.data.data.cgpa||'', skills:(p.data.data.skills||[]).join(', '), resumeUrl:p.data.data.resumeUrl||'' }); }
      setApps(a.data.data ?? []); setInterviews(i.data.data ?? []); setOffers(o.data.data ?? []);
    }catch{}
  };
  useEffect(()=>{ loadAll(); },[]);

  const saveProfile=async(e)=>{
    e.preventDefault();
    try{
      const payload={
        college: form.college||null,
        department: form.department||null,
        year: form.year? Number(form.year):null,
        cgpa: form.cgpa? Number(form.cgpa):null,
        skills: form.skills? form.skills.split(',').map(s=>s.trim()).filter(Boolean):[],
        resumeUrl: form.resumeUrl||null,
      };
      const { data } = await api.put('/students/me', payload);
      setProfile(data.data); toast.ok('Profile saved ✓');
    }catch(ex){ toast.err(ex.response?.data?.message||'Save failed'); }
  };
  const withdraw=async(id)=>{
    try{ await api.post(`/applications/${id}/withdraw`); toast.ok('Withdrawn'); loadAll(); }catch(ex){ toast.err(ex.response?.data?.message||'Failed'); }
  };
  const respondOffer=async(id, accept)=>{
    try{ await api.post(`/offers/${id}/respond`, { accept }); toast.ok(accept?'Offer accepted 🎉':'Offer declined'); loadAll(); }catch(ex){ toast.err(ex.response?.data?.message||'Failed'); }
  };

  if(!user || user.role!=='STUDENT') return <div className="container page"><div className="card empty"><b>Students only</b>Please sign in as Student.</div></div>;

  return (
    <div className="container page">
      <h2 className="section-title">Student workspace</h2>
      <p className="section-sub">Profile · Applications · Interviews · Offers — all in one place.</p>
      <div className="tabs" style={{marginTop:16}}>
        {['profile','applications','interviews','offers'].map(t=>(
          <button key={t} className={`tab ${tab===t?'active':''}`} onClick={()=>setTab(t)}>{t[0].toUpperCase()+t.slice(1)}</button>
        ))}
      </div>

      {tab==='profile' && (
        <div className="grid" style={{marginTop:16}}>
          <div className="card" style={{gridColumn:'span 7'}}>
            <h3>Edit profile</h3>
            <p className="muted" style={{fontSize:13,margin:'6px 0 14px'}}>Keep CGPA and skills accurate — they drive match scores and eligibility gates.</p>
            <form onSubmit={saveProfile} className="form">
              <div className="form-row">
                <div><label className="label">College</label><input className="input" value={form.college} onChange={e=>setForm({...form,college:e.target.value})} placeholder="PICT Pune" /></div>
                <div><label className="label">Department</label><input className="input" value={form.department} onChange={e=>setForm({...form,department:e.target.value})} placeholder="CSE" /></div>
              </div>
              <div className="form-row">
                <div><label className="label">Graduation year</label><input className="input" type="number" value={form.year} onChange={e=>setForm({...form,year:e.target.value})} placeholder="2026" /></div>
                <div><label className="label">CGPA (0–10)</label><input className="input" type="number" step="0.01" value={form.cgpa} onChange={e=>setForm({...form,cgpa:e.target.value})} placeholder="8.2" /></div>
              </div>
              <div><label className="label">Skills (comma-separated)</label><input className="input" value={form.skills} onChange={e=>setForm({...form,skills:e.target.value})} placeholder="Java, Spring, React, SQL" /></div>
              <div><label className="label">Resume URL</label><input className="input" value={form.resumeUrl} onChange={e=>setForm({...form,resumeUrl:e.target.value})} placeholder="https://…/resume.pdf" /></div>
              <button className="btn btn-primary">Save profile</button>
            </form>
          </div>
          <div style={{gridColumn:'span 5',display:'grid',gap:12}}>
            <div className="card">
              <h3>Your card</h3>
              {profile ? (
                <div style={{marginTop:10,display:'grid',gap:8,fontSize:13}}>
                  <div><span className="muted">Name</span><div style={{fontWeight:700}}>{profile.name}</div></div>
                  <div><span className="muted">Email</span><div>{profile.email}</div></div>
                  <div><span className="muted">College</span><div>{profile.college||'—'}</div></div>
                  <div className="chips">{(profile.skills||[]).map(s=> <span key={s} className="chip">{s}</span>)}</div>
                  {profile.resumeUrl && <a href={profile.resumeUrl} target="_blank" rel="noreferrer" style={{fontSize:13}}>View resume →</a>}
                </div>
              ) : <p className="muted" style={{fontSize:13}}>Save profile to see your card.</p>}
            </div>
            <div className="card">
              <b style={{fontSize:13,letterSpacing:'.06em',textTransform:'uppercase',color:'var(--muted)'}}>At a glance</b>
              <div style={{display:'grid',gridTemplateColumns:'1fr 1fr',gap:10,marginTop:10}}>
                <div className="stat"><b>{apps.length}</b><span>Applications</span></div>
                <div className="stat"><b>{interviews.length}</b><span>Interviews</span></div>
                <div className="stat"><b>{offers.length}</b><span>Offers</span></div>
                <div className="stat"><b>{profile?.cgpa ?? '—'}</b><span>CGPA</span></div>
              </div>
            </div>
          </div>
        </div>
      )}

      {tab==='applications' && (
        <div className="table-wrap" style={{marginTop:16}}>
          {apps.length===0 ? <div className="empty"><b>No applications yet</b>Go to Jobs and apply — your match % is shown on each card.</div> : (
            <table>
              <thead><tr><th>Job</th><th>Company</th><th>Status</th><th>Applied</th><th></th></tr></thead>
              <tbody>
                {apps.map(a=>(
                  <tr key={a.id}>
                    <td><b>{a.jobTitle}</b></td>
                    <td className="muted">{a.companyName}</td>
                    <td><span className={`badge ${statusBadge(a.status)}`}>{a.status}</span></td>
                    <td className="muted" style={{fontSize:12}}>{fmtDate(a.appliedAt)}</td>
                    <td>{['APPLIED','SHORTLISTED','INTERVIEW'].includes(a.status) && <button className="btn btn-ghost btn-sm" onClick={()=>withdraw(a.id)}>Withdraw</button>}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {tab==='interviews' && (
        <div className="table-wrap" style={{marginTop:16}}>
          {interviews.length===0 ? <div className="empty"><b>No interviews scheduled</b>When a company schedules one, it appears here.</div> : (
            <table>
              <thead><tr><th>Application</th><th>When</th><th>Mode</th><th>Link / Interviewer</th><th>Status</th></tr></thead>
              <tbody>
                {interviews.map(iv=>(
                  <tr key={iv.id}>
                    <td>#{iv.applicationId}</td>
                    <td>{fmtDate(iv.scheduledAt)}</td>
                    <td><span className="badge">{iv.mode}</span></td>
                    <td className="muted" style={{fontSize:12}}>{iv.meetingLink ? <a href={iv.meetingLink} target="_blank" rel="noreferrer">Join</a> : ''} {iv.interviewerName ? `· ${iv.interviewerName}`:''}</td>
                    <td><span className={`badge ${statusBadge(iv.status)}`}>{iv.status}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {tab==='offers' && (
        <div className="table-wrap" style={{marginTop:16}}>
          {offers.length===0 ? <div className="empty"><b>No offers yet</b>Keep applications active — companies issue offers here.</div> : (
            <table>
              <thead><tr><th>Application</th><th>Salary</th><th>Joining</th><th>Status</th><th></th></tr></thead>
              <tbody>
                {offers.map(o=>(
                  <tr key={o.id}>
                    <td>#{o.applicationId}</td>
                    <td>{fmtSalary(o.salary)}</td>
                    <td>{o.joiningDate ? fmtDate(o.joiningDate) : '—'}</td>
                    <td><span className={`badge ${statusBadge(o.status)}`}>{o.status}</span></td>
                    <td style={{display:'flex',gap:6}}>
                      {o.status==='PENDING' && (
                        <>
                          <button className="btn btn-primary btn-sm" onClick={()=>respondOffer(o.id,true)}>Accept</button>
                          <button className="btn btn-ghost btn-sm" onClick={()=>respondOffer(o.id,false)}>Decline</button>
                        </>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  );
}

// ── Company ──────────────────────────────────────────────────────────
function CompanyPage({ user }){
  const toast=useToast();
  const [tab,setTab]=useState('jobs');
  const [profile,setProfile]=useState(null); const [pForm,setPForm]=useState({ name:'', description:'', website:'', industry:'', location:'' });
  const [jobs,setJobs]=useState([]); const [apps,setApps]=useState([]);
  const [showPost,setShowPost]=useState(false);
  const [post,setPost]=useState({ title:'SDE Intern', description:'Build with Java + Spring Boot. Mentored, real prod.', location:'Bengaluru', jobType:'INTERNSHIP', salary:'60000', minimumCgpa:'7.0', requiredSkills:'Java,Spring,SQL', deadline: new Date(Date.now()+ 14*86400000).toISOString().slice(0,16) });

  const load=async()=>{
    try{
      const [me,mine,ca]=await Promise.all([
        api.get('/companies/me').catch(()=>null),
        api.get('/jobs/mine').catch(()=>({data:{data:[]}})),
        api.get('/applications/company').catch(()=>({data:{data:[]}})),
      ]);
      if(me?.data?.data){ setProfile(me.data.data); setPForm({ name:me.data.data.name||'', description:me.data.data.description||'', website:me.data.data.website||'', industry:me.data.data.industry||'', location:me.data.data.location||'' }); }
      setJobs(mine.data.data ?? []); setApps(ca.data.data ?? []);
    }catch{}
  };
  useEffect(()=>{ load(); },[]);

  const saveProfile=async(e)=>{
    e.preventDefault();
    try{ const {data}=await api.put('/companies/me', { name:pForm.name, description:pForm.description, website:pForm.website, industry:pForm.industry, location:pForm.location }); setProfile(data.data); toast.ok('Company profile saved'); }catch(ex){ toast.err(ex.response?.data?.message||'Save failed'); }
  };
  const createJob=async(e)=>{
    e.preventDefault();
    try{
      const payload={
        title: post.title, description: post.description, location: post.location, jobType: post.jobType,
        salary: post.salary? Number(post.salary):null,
        minimumCgpa: post.minimumCgpa? Number(post.minimumCgpa):null,
        requiredSkills: post.requiredSkills,
        deadline: post.deadline? new Date(post.deadline).toISOString():null,
      };
      await api.post('/jobs', payload); toast.ok('Job posted ✓'); setShowPost(false); load();
    }catch(ex){ toast.err(ex.response?.data?.message||ex.response?.data?.error||'Post failed'); }
  };
  const closeJob=async(id)=>{ try{ await api.post(`/jobs/${id}/close`); toast.ok('Job closed'); load(); }catch(ex){ toast.err(ex.response?.data?.message||'Close failed'); } };
  const updateAppStatus=async(id, status)=>{ try{ await api.patch(`/applications/${id}/status`, { status }); toast.ok(`Moved to ${status}`); load(); }catch(ex){ toast.err(ex.response?.data?.message||'Update failed'); } };
  const scheduleInterview=async(appId)=>{
    const scheduledAt = new Date(Date.now()+ 2*86400000).toISOString();
    try{ await api.post('/interviews', { applicationId: appId, scheduledAt, mode:'ONLINE', meetingLink:'https://meet.example.com/'+appId, interviewerName: user?.name||'Hiring Manager' }); toast.ok('Interview scheduled'); }catch(ex){ toast.err(ex.response?.data?.message||'Schedule failed'); }
  };
  const issueOffer=async(appId)=>{
    try{ await api.post('/offers', { applicationId: appId, salary: 80000, joiningDate: new Date(Date.now()+ 30*86400000).toISOString().slice(0,10) }); toast.ok('Offer issued'); }catch(ex){ toast.err(ex.response?.data?.message||'Offer failed'); }
  };

  if(!user || user.role!=='COMPANY') return <div className="container page"><div className="card empty"><b>Companies only</b>Sign in as Company to manage jobs.</div></div>;

  return (
    <div className="container page">
      <div style={{display:'flex',justifyContent:'space-between',alignItems:'center',flexWrap:'wrap',gap:12}}>
        <div>
          <h2 className="section-title">Company console</h2>
          <p className="section-sub">{profile?.verified ? '✓ Verified' : '⏳ Pending verification by Admin'} · {jobs.length} jobs · {apps.length} applicants</p>
        </div>
        <button className="btn btn-primary" onClick={()=>setShowPost(true)}>+ Post a job</button>
      </div>

      <div className="tabs" style={{marginTop:16}}>
        {['jobs','applicants','profile'].map(t=>(
          <button key={t} className={`tab ${tab===t?'active':''}`} onClick={()=>setTab(t)}>{t[0].toUpperCase()+t.slice(1)}</button>
        ))}
      </div>

      {tab==='profile' && (
        <div className="card" style={{marginTop:16,padding:20}}>
          <h3>Company profile</h3>
          <form onSubmit={saveProfile} className="form" style={{marginTop:12}}>
            <div><label className="label">Company name</label><input className="input" value={pForm.name} onChange={e=>setPForm({...pForm,name:e.target.value})} /></div>
            <div><label className="label">Description</label><textarea className="textarea" value={pForm.description} onChange={e=>setPForm({...pForm,description:e.target.value})} placeholder="What you build…" /></div>
            <div className="form-row">
              <div><label className="label">Website</label><input className="input" value={pForm.website} onChange={e=>setPForm({...pForm,website:e.target.value})} placeholder="https://" /></div>
              <div><label className="label">Industry</label><input className="input" value={pForm.industry} onChange={e=>setPForm({...pForm,industry:e.target.value})} placeholder="Software" /></div>
            </div>
            <div><label className="label">Headquarters</label><input className="input" value={pForm.location} onChange={e=>setPForm({...pForm,location:e.target.value})} placeholder="Bengaluru" /></div>
            <button className="btn btn-primary" style={{width:'fit-content'}}>Save profile</button>
          </form>
        </div>
      )}

      {tab==='jobs' && (
        <div style={{marginTop:16,display:'grid',gap:12}}>
          {jobs.length===0 ? <div className="card empty"><b>No jobs yet</b>Post your first role — it goes live immediately.</div> : jobs.map(j=>(
            <div key={j.id} className="card" style={{display:'flex',justifyContent:'space-between',gap:12,alignItems:'center',flexWrap:'wrap'}}>
              <div>
                <div style={{display:'flex',gap:8,alignItems:'center'}}><b>{j.title}</b><span className={`badge ${statusBadge(j.status)}`}>{j.status}</span><span className="muted" style={{fontSize:12}}>{j.location} · {j.jobType}</span></div>
                <div className="muted" style={{fontSize:13,marginTop:4,display:'flex',gap:8,flexWrap:'wrap'}}><span>Deadline {fmtDate(j.deadline)}</span><span>CGPA ≥ {j.minimumCgpa ?? '—'}</span><span>{fmtSalary(j.salary)}</span></div>
                <div className="chips">{(j.requiredSkills||'').split(',').filter(Boolean).map(s=> <span key={s} className="chip">{s.trim()}</span>)}</div>
              </div>
              <div style={{display:'flex',gap:8}}>
                <Link to={`/jobs`} className="btn btn-ghost btn-sm">View public</Link>
                {j.status==='OPEN' && <button className="btn btn-ghost btn-sm" onClick={()=>closeJob(j.id)}>Close</button>}
              </div>
            </div>
          ))}
        </div>
      )}

      {tab==='applicants' && (
        <div className="table-wrap" style={{marginTop:16}}>
          {apps.length===0 ? <div className="empty"><b>No applicants yet</b>Share your job link and check back.</div> : (
            <table>
              <thead><tr><th>Student</th><th>Job</th><th>Status</th><th>Actions</th></tr></thead>
              <tbody>
                {apps.map(a=>(
                  <tr key={a.id}>
                    <td><b>{a.studentName}</b><div className="muted" style={{fontSize:12}}>#{a.studentId}</div></td>
                    <td>{a.jobTitle}</td>
                    <td><span className={`badge ${statusBadge(a.status)}`}>{a.status}</span></td>
                    <td style={{display:'flex',gap:6,flexWrap:'wrap'}}>
                      <select className="select" style={{maxWidth:150,padding:'6px 8px'}} defaultValue="" onChange={e=> e.target.value && updateAppStatus(a.id, e.target.value)}>
                        <option value="">Move to…</option>
                        <option value="SHORTLISTED">Shortlisted</option>
                        <option value="INTERVIEW">Interview</option>
                        <option value="OFFERED">Offered</option>
                        <option value="REJECTED">Rejected</option>
                      </select>
                      <button className="btn btn-ghost btn-sm" onClick={()=>scheduleInterview(a.id)}>Interview</button>
                      <button className="btn btn-primary btn-sm" onClick={()=>issueOffer(a.id)}>Offer</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
          <div className="muted" style={{padding:'10px 14px',fontSize:12,borderTop:'1px solid var(--border)'}}>Tip: status moves forward only (BR-05). You can also use the quick Interview/Offer buttons — they schedule for +2 days / +30 days.</div>
        </div>
      )}

      {showPost && (
        <div className="overlay" onClick={()=>setShowPost(false)}>
          <div className="modal" onClick={e=>e.stopPropagation()}>
            <div className="modal-head"><b>Post a new job</b><button className="icon-btn" onClick={()=>setShowPost(false)}>✕</button></div>
            <form onSubmit={createJob} className="modal-body form">
              <div><label className="label">Title</label><input className="input" value={post.title} onChange={e=>setPost({...post,title:e.target.value})} required /></div>
              <div><label className="label">Description</label><textarea className="textarea" value={post.description} onChange={e=>setPost({...post,description:e.target.value})} required /></div>
              <div className="form-row">
                <div><label className="label">Location</label><input className="input" value={post.location} onChange={e=>setPost({...post,location:e.target.value})} /></div>
                <div><label className="label">Type</label><select className="select" value={post.jobType} onChange={e=>setPost({...post,jobType:e.target.value})}><option value="INTERNSHIP">Internship</option><option value="FULL_TIME">Full-time</option><option value="PART_TIME">Part-time</option></select></div>
              </div>
              <div className="form-row">
                <div><label className="label">Salary (₹ / mo)</label><input className="input" type="number" value={post.salary} onChange={e=>setPost({...post,salary:e.target.value})} /></div>
                <div><label className="label">CGPA floor</label><input className="input" type="number" step="0.1" value={post.minimumCgpa} onChange={e=>setPost({...post,minimumCgpa:e.target.value})} /></div>
              </div>
              <div><label className="label">Required skills (comma-separated)</label><input className="input" value={post.requiredSkills} onChange={e=>setPost({...post,requiredSkills:e.target.value})} placeholder="Java,Spring,React" /></div>
              <div><label className="label">Deadline</label><input className="input" type="datetime-local" value={post.deadline} onChange={e=>setPost({...post,deadline:e.target.value})} /></div>
              <button className="btn btn-primary">Publish job</button>
              <p className="muted" style={{fontSize:12,margin:0}}>Deadline is enforced (BR-03). CGPA checked on apply (BR-04). Duplicate apply blocked (BR-01).</p>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

// ── Admin ────────────────────────────────────────────────────────────
function AdminPage({ user }){
  const [stats,setStats]=useState(null); const [companies,setCompanies]=useState([]); const toast=useToast();
  const [activeKey,setActiveKey]=useState(null);
  const [drillTitle,setDrillTitle]=useState('');
  const [drillData,setDrillData]=useState([]);
  const [drillLoading,setDrillLoading]=useState(false);

  const load=async()=>{
    try{
      const [s,c]=await Promise.all([ api.get('/admin/stats'), api.get('/admin/companies') ]);
      setStats(s.data.data); setCompanies(c.data.data ?? []);
    }catch(ex){ toast.err('Admin access required'); }
  };
  useEffect(()=>{ if(user?.role==='ADMIN') load(); },[user]);

  const toggleVerify=async(c)=>{
    try{
      const { data } = await api.post(`/admin/companies/${c.id}/verify`, { verified: !c.verified });
      setCompanies(cs=> cs.map(x=> x.id===c.id ? data.data : x));
      // keep drill in sync if viewing companies
      if(activeKey==='companies' || activeKey==='unverifiedCompanies'){
        setDrillData(d=> d.map(x=> x.id===c.id ? data.data : x));
      }
      toast.ok(data.data.verified? 'Company verified ✓':'Verification removed');
      // refresh stats after verify
      api.get('/admin/stats').then(r=> setStats(r.data.data)).catch(()=>{});
    }catch(ex){ toast.err(ex.response?.data?.message||'Failed'); }
  };

  const handleStatClick=async(key)=>{
    if(activeKey===key){
      setActiveKey(null); setDrillData([]); setDrillTitle(''); return;
    }
    setActiveKey(key);
    setDrillLoading(true);
    setDrillData([]);
    // map key -> title + fetcher
    let title=''; let fetcher=null;
    if(key==='users'){ title='Users'; fetcher=()=>api.get('/admin/users'); }
    else if(key==='companies'){ title='All companies'; fetcher=()=>api.get('/admin/companies'); }
    else if(key==='unverifiedCompanies'){ title='Unverified companies'; fetcher=()=>api.get('/admin/companies',{params:{verified:false}}); }
    else if(key==='openJobs'){ title='Open jobs'; fetcher=()=>api.get('/admin/jobs',{params:{status:'OPEN'}}); }
    else if(key==='totalJobs'){ title='All jobs'; fetcher=()=>api.get('/admin/jobs'); }
    else if(key.startsWith('applications_')){
      const raw=key.slice('applications_'.length); // e.g. applied
      const status=raw.toUpperCase();
      title=`Applications — ${status}`;
      fetcher=()=>api.get('/admin/applications',{params:{status}});
    } else { title=key; fetcher=()=>api.get('/admin/stats'); }

    setDrillTitle(title);
    try{
      const { data } = await fetcher();
      const rows = data.data ?? [];
      setDrillData(Array.isArray(rows)?rows:[]);
    }catch(ex){
      const msg=ex.response?.data?.message||'Failed to load '+title;
      toast.err(msg);
      setDrillData([]);
    }finally{ setDrillLoading(false); }
  };

  const fmtAppStatus=(s)=> statusBadge(s);

  const renderDrill=()=>{
    if(!activeKey) return null;
    if(drillLoading){
      return (
        <div className="table-wrap drill">
          <div className="drill-head"><span className="drill-title">{drillTitle}</span><span className="drill-count">Loading…</span></div>
          <div style={{padding:20,display:'grid',gap:10}}>
            <div className="skeleton" style={{height:14}}/><div className="skeleton" style={{height:14,width:'80%'}}/><div className="skeleton" style={{height:14,width:'60%'}}/>
          </div>
        </div>
      );
    }
    if(activeKey==='users'){
      return (
        <div className="table-wrap drill">
          <div className="drill-head"><span className="drill-title">{drillTitle}</span><span className="drill-count">{drillData.length} records</span><button className="btn btn-ghost btn-sm" onClick={()=>setActiveKey(null)}>Close ✕</button></div>
          {drillData.length===0 ? <div className="empty"><b>No users</b>Users appear after registration.</div> : (
            <table>
              <thead><tr><th>#</th><th>Name</th><th>Email</th><th>Role</th><th>Status</th></tr></thead>
              <tbody>{drillData.map(u=>(
                <tr key={u.id}><td className="muted" style={{fontSize:12}}>#{u.id}</td><td><b>{u.name}</b></td><td className="muted" style={{fontSize:12}}>{u.email}</td><td><span className={`badge ${u.role==='ADMIN'?'primary':u.role==='COMPANY'?'warning':'success'}`}>{u.role}</span></td><td><span className={`badge ${u.enabled?'success':'danger'}`}>{u.enabled?'Active':'Disabled'}</span></td></tr>
              ))}</tbody>
            </table>
          )}
        </div>
      );
    }
    if(activeKey==='companies' || activeKey==='unverifiedCompanies'){
      return (
        <div className="table-wrap drill">
          <div className="drill-head"><span className="drill-title">{drillTitle}</span><span className="drill-count">{drillData.length} records</span><button className="btn btn-ghost btn-sm" onClick={()=>setActiveKey(null)}>Close ✕</button></div>
          {drillData.length===0 ? <div className="empty"><b>No companies</b>They appear after registration.</div> : (
            <table>
              <thead><tr><th>Company</th><th>Email</th><th>Location</th><th>Verified</th><th></th></tr></thead>
              <tbody>{drillData.map(c=>(
                <tr key={c.id}>
                  <td><b>{c.name}</b><div className="muted" style={{fontSize:12}}>{c.industry||'—'}</div></td>
                  <td className="muted" style={{fontSize:12}}>{c.email}</td>
                  <td>{c.location||'—'}</td>
                  <td><span className={`badge ${c.verified?'success':'warning'}`}>{c.verified?'Verified':'Pending'}</span></td>
                  <td><button className={`btn btn-sm ${c.verified?'btn-ghost':'btn-primary'}`} onClick={()=>toggleVerify(c)}>{c.verified?'Revoke':'Verify'}</button></td>
                </tr>
              ))}</tbody>
            </table>
          )}
        </div>
      );
    }
    if(activeKey==='openJobs' || activeKey==='totalJobs'){
      return (
        <div className="table-wrap drill">
          <div className="drill-head"><span className="drill-title">{drillTitle}</span><span className="drill-count">{drillData.length} records</span><button className="btn btn-ghost btn-sm" onClick={()=>setActiveKey(null)}>Close ✕</button></div>
          {drillData.length===0 ? <div className="empty"><b>No jobs</b>Post a job from the Company console.</div> : (
            <table>
              <thead><tr><th>Job</th><th>Company</th><th>Type</th><th>Status</th><th>Deadline</th><th>Salary</th></tr></thead>
              <tbody>{drillData.map(j=>(
                <tr key={j.id}>
                  <td><b>{j.title}</b><div className="muted" style={{fontSize:12,display:'-webkit-box',WebkitLineClamp:1,WebkitBoxOrient:'vertical',overflow:'hidden'}}>{j.description?.slice(0,60) || ''}</div></td>
                  <td>{j.companyName}</td>
                  <td><span className="badge">{j.jobType?.replace('_',' ')}</span></td>
                  <td><span className={`badge ${statusBadge(j.status)}`}>{j.status}</span></td>
                  <td className="muted" style={{fontSize:12}}>{fmtDate(j.deadline)}</td>
                  <td>{fmtSalary(j.salary)}</td>
                </tr>
              ))}</tbody>
            </table>
          )}
        </div>
      );
    }
    if(activeKey.startsWith('applications_')){
      return (
        <div className="table-wrap drill">
          <div className="drill-head"><span className="drill-title">{drillTitle}</span><span className="drill-count">{drillData.length} records</span><button className="btn btn-ghost btn-sm" onClick={()=>setActiveKey(null)}>Close ✕</button></div>
          {drillData.length===0 ? <div className="empty"><b>No applications</b>No records for this status yet.</div> : (
            <table>
              <thead><tr><th>#</th><th>Student</th><th>Job</th><th>Company</th><th>Status</th><th>Applied</th></tr></thead>
              <tbody>{drillData.map(a=>(
                <tr key={a.id}>
                  <td className="muted" style={{fontSize:12}}>#{a.id}</td>
                  <td><b>{a.studentName}</b><div className="muted" style={{fontSize:11}}>#{a.studentId}</div></td>
                  <td>{a.jobTitle}<div className="muted" style={{fontSize:11}}>#{a.jobId}</div></td>
                  <td>{a.companyName}</td>
                  <td><span className={`badge ${fmtAppStatus(a.status)}`}>{a.status}</span></td>
                  <td className="muted" style={{fontSize:12}}>{fmtDate(a.appliedAt)}</td>
                </tr>
              ))}</tbody>
            </table>
          )}
        </div>
      );
    }
    return null;
  };

  if(!user || user.role!=='ADMIN') return <div className="container page"><div className="card empty"><b>Admins only</b>Sign in with an ADMIN account.</div></div>;
  return (
    <div className="container page">
      <h2 className="section-title">Admin control center</h2>
      <p className="section-sub">Verify companies · monitor platform health · audit trail via backend /api/health. Click any card to drill into live data.</p>
      {stats && (
        <div className="grid" style={{marginTop:16}}>
          {Object.entries(stats).map(([k,v])=>(
            <button key={k} onClick={()=>handleStatClick(k)} className={`card stat-card ${activeKey===k?'active':''}`} style={{gridColumn:'span 3',textAlign:'left',width:'100%',padding:18}}>
              <div className="muted" style={{fontSize:11,letterSpacing:'.08em',textTransform:'uppercase',fontWeight:800}}>{k}</div>
              <b style={{fontSize:28,display:'block',marginTop:4}}>{String(v)}</b>
              <div className="stat-hint">{activeKey===k ? '● viewing · click to close' : '↗ click to view'}</div>
            </button>
          ))}
        </div>
      )}

      {renderDrill()}

      <div className="table-wrap" style={{marginTop:16}}>
        <div style={{padding:'14px 16px',display:'flex',justifyContent:'space-between',alignItems:'center',borderBottom:'1px solid var(--border)'}}>
          <b>Companies</b><button className="btn btn-ghost btn-sm" onClick={load}>Refresh</button>
        </div>
        {companies.length===0 ? <div className="empty"><b>No companies</b>They appear after registration.</div> : (
          <table>
            <thead><tr><th>Company</th><th>Email</th><th>Location</th><th>Verified</th><th></th></tr></thead>
            <tbody>
              {companies.map(c=>(
                <tr key={c.id}>
                  <td><b>{c.name}</b><div className="muted" style={{fontSize:12}}>{c.industry || '—'}</div></td>
                  <td className="muted" style={{fontSize:12}}>{c.email}</td>
                  <td>{c.location||'—'}</td>
                  <td><span className={`badge ${c.verified?'success':'warning'}`}>{c.verified?'Verified':'Pending'}</span></td>
                  <td><button className={`btn btn-sm ${c.verified?'btn-ghost':'btn-primary'}`} onClick={()=>toggleVerify(c)}>{c.verified?'Revoke':'Verify'}</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

function NotificationsPage(){
  const [items,setItems]=useState([]);
  useEffect(()=>{ api.get('/notifications/mine').then(r=> setItems(r.data.data ?? [])).catch(()=>{}); },[]);
  return (
    <div className="container page" style={{maxWidth:720}}>
      <h2 className="section-title">Notifications</h2>
      <p className="section-sub">In-app updates from applications, interviews and offers.</p>
      <div style={{marginTop:16,display:'grid',gap:10}}>
        {items.length===0 ? <div className="card empty"><b>All caught up</b>No notifications yet.</div> : items.map(n=>(
          <div key={n.id||n.title} className="card" style={{padding:14}}>
            <b style={{fontSize:14}}>{n.title}</b>
            <div className="muted" style={{fontSize:13,marginTop:4}}>{n.message}</div>
            <div className="muted" style={{fontSize:11,marginTop:8}}>{n.createdAt ? fmtDate(n.createdAt) : ''}</div>
          </div>
        ))}
      </div>
    </div>
  );
}

function Footer(){
  return (
    <footer className="footer">
      <div className="container" style={{display:'flex',justifyContent:'space-between',gap:12,flexWrap:'wrap'}}>
        <span>© {new Date().getFullYear()} CampusHire — Internship & Campus Hiring Platform · Java 17 · Spring Boot 3 · PostgreSQL · React</span>
        <span style={{display:'flex',gap:12}}><a href="/api/health" target="_blank" rel="noreferrer">Health</a><a href="/swagger-ui.html" target="_blank" rel="noreferrer">API Docs</a></span>
      </div>
    </footer>
  );
}

// ── app ──────────────────────────────────────────────────────────────
export default function App(){
  const { user, setUser, logout } = useAuthState();
  const doLogout=()=>{
    logout();
    window.location.href='/';
  };
  return (
    <ToastProvider>
      <Header user={user} logout={doLogout} />
      <main style={{minHeight:'calc(100vh - 64px - 60px)'}}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login setUser={setUser} />} />
          <Route path="/register" element={<Register />} />
          <Route path="/jobs" element={<Jobs user={user} />} />
          <Route path="/student" element={<StudentPage user={user} />} />
          <Route path="/company" element={<CompanyPage user={user} />} />
          <Route path="/admin" element={<AdminPage user={user} />} />
          <Route path="/notifications" element={<NotificationsPage />} />
          <Route path="*" element={<div className="container page"><div className="card empty"><b>Not found</b><Link to="/">Go home</Link></div></div>} />
        </Routes>
      </main>
      <Footer />
    </ToastProvider>
  );
}
