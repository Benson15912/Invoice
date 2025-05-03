import { BrowserRouter as Router, Routes, Route, Link } from "react-router-dom";
import InvoiceView from "./InvoiceView.jsx";
import './App.css';

function App() {
  return (
    <Router>
      <div className="app-container">
        <nav className="navbar">
          <div className="nav-logo">My App</div>
          <div className="nav-links">
            <Link to="/">Home</Link>
            <Link to="/invoice">Invoice</Link>
          </div>
        </nav>
        <div className="content">
          <Routes>
            <Route path="/" />
            <Route path="/invoice" element={<InvoiceView />} />
          </Routes>
        </div>
      </div>
    </Router>
  );
}

export default App;
