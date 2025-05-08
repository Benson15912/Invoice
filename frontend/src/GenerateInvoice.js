import { useState } from "react";
import "./invoice.css";

function GenerateInvoice() {
    const [form, setForm] = useState({
        studentId: "",
        name: "",
        rate: "",
        date: "",
        numberOfLessons: ""
    });

    const [invoiceUrl, setInvoiceUrl] = useState("");

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm({ ...form, [name]: value });
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        const params = new URLSearchParams({
            studentId: form.studentId,
            name: form.name,
            rate: form.rate,
            date: form.date,
            numberOfLessons: form.numberOfLessons
        });

        const url = `http://localhost:8080/api/invoices/generateinvoice/manual`;

        fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: params.toString()
        })
            .then(response => {
                if (response.ok) {
                    return response.json();  // Parse the JSON response
                } else {
                    throw new Error('Failed to generate invoice');
                }
            })
            .then(data => {
                // Access the returned data (URL in this case)
                const filePath = data.filePath;
                const invoiceUrl = `http://localhost:8080/api/storage/view?filepath=${encodeURIComponent(filePath)}`;
                console.log('Invoice URL:', invoiceUrl);
                setInvoiceUrl(invoiceUrl);  // Use the returned URL (or handle it however needed)
            })
            .catch(error => {
                console.error('Error:', error);
            });
    };

    return (
        <div className="generate-invoice-page">
            <form className="invoice-form" onSubmit={handleSubmit}>
                <h2>Generate Invoice</h2>
                <label>
                    Student ID:
                    <input type="number" name="studentId" value={form.studentId} onChange={handleChange} required />
                </label>
                <label>
                    Name:
                    <input type="text" name="name" value={form.name} onChange={handleChange} required />
                </label>
                <label>
                    Rate:
                    <input type="number" name="rate" value={form.rate} onChange={handleChange} required />
                </label>
                <label>
                    Date:
                    <input type="" name="date" value={form.date} onChange={handleChange} required />
                </label>
                <label>
                    Number of Lessons:
                    <input type="number" name="numberOfLessons" value={form.numberOfLessons} onChange={handleChange} required />
                </label>
                <button type="submit">Generate</button>
            </form>

            {invoiceUrl && (
                <div className="invoice-preview">
                    <embed src={invoiceUrl} width="100%" height="600px" type="application/pdf" />
                </div>
            )}
        </div>
    );
}

export default GenerateInvoice;
