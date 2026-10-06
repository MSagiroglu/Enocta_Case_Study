const express = require('express');
const app = express();
app.use(express.json());

app.post('/token', (req, res) => {
    if (req.headers.user && req.headers.pass) {
        res.json({ token: "sample_token_123" });
    } else {
        res.status(401).send();
    }
});

app.get('/viewInvoice', (req, res) => {
    const barcode = req.query.barcode;
    res.json({
        InvoiceLink: "http://abc.com/invoice.pdf",
        Result: { success: true }
    });
});

app.post('/sendInvoice', (req, res) => {
    if (req.headers.token === "sample_token_123" && req.body.Barcode) {
        res.json({ success: true, receivedBarcode: req.body.Barcode.barcode });
    } else {
        res.status(401).send();
    }
});

app.listen(3000, () => console.log('Mock Server running on 3000'));
