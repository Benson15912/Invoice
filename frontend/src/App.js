import { BrowserRouter as Router, Routes, Route, Link } from "react-router-dom";
import InvoiceView from "./InvoiceView.jsx";
import './App.css';
import GenerateInvoice from "./GenerateInvoice";

function App() {
  return (
    <Router>
      <div>
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
            <Route path="/generate-invoice" element={<GenerateInvoice />} />
          </Routes>
        </div>
      </div>
    </Router>
  );
}

export default App;
