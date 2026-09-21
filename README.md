# 🔍 AI OCR Scanner

> **Intelligent document and image text extraction powered by AI & OCR**

AI OCR Scanner is a modern OCR solution designed to automatically detect, extract, process, and structure text from **images, scanned documents, invoices, identity documents, forms, and PDFs**.

The project combines **Optical Character Recognition (OCR)**, image preprocessing, AI-powered text processing, and a modern web/mobile interface to transform unstructured documents into usable digital data.

---

## ✨ Features

### 📄 Document Scanning

* Upload images and PDF documents
* Capture documents using a camera
* Automatic document detection
* Image quality optimization
* Multi-page document processing

### 🤖 AI-Powered OCR

* Automatic text detection
* Text extraction from images
* Character recognition
* Layout-aware extraction
* Structured data extraction
* Support for printed documents
* Support for multiple languages

### 🧠 Intelligent Document Processing

The system can identify and organize extracted information such as:

* Names
* Dates
* Phone numbers
* Email addresses
* Addresses
* Identification numbers
* Invoice numbers
* Prices
* Tables
* Custom fields

### 🖼️ Image Processing

Before OCR processing, the system can apply:

* Image resizing
* Noise reduction
* Contrast enhancement
* Grayscale conversion
* Rotation correction
* Perspective correction
* Document cropping
* Image sharpening

### 📊 Results & Export

Users can:

* View extracted text
* Edit OCR results
* Copy extracted content
* Download results
* Export structured data
* Search inside documents
* Save processed documents

Supported export formats can include:

```text
TXT
JSON
CSV
PDF
DOCX
```

---

# 🎯 Use Cases

AI OCR Scanner can be used for:

### 🏢 Businesses

* Invoice processing
* Contract digitization
* Document archiving
* Customer information extraction
* Administrative automation

### 🎓 Education

* Digitizing lecture notes
* Extracting text from books
* Converting scanned documents
* Processing student documents

### 🏥 Healthcare

* Medical document digitization
* Patient information extraction
* Prescription processing
* Medical archive management

### 🏦 Finance

* Invoice processing
* Receipt recognition
* Financial document extraction
* Expense management

### 🏛️ Administration

* Form processing
* Identity document extraction
* Digital archiving
* Automated data entry

---

# 🏗️ System Architecture

```text
                    ┌─────────────────────┐
                    │       User          │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ Web / Mobile Client │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │     REST API        │
                    │  Authentication     │
                    └──────────┬──────────┘
                               │
              ┌────────────────┼────────────────┐
              │                │                │
              ▼                ▼                ▼
       ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
       │ Image       │  │ OCR Engine  │  │ AI Engine   │
       │ Processing  │  │             │  │             │
       └──────┬──────┘  └──────┬──────┘  └──────┬──────┘
              │                │                │
              └────────────────┼────────────────┘
                               ▼
                    ┌─────────────────────┐
                    │ Structured Results  │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ Database / Storage   │
                    └─────────────────────┘
```

---

# 🛠️ Technology Stack

The technology stack can be adapted depending on the deployment requirements.

### Frontend

* Flutter
* React
* HTML5 / CSS3
* JavaScript / TypeScript

### Backend

* Python
* FastAPI
* REST API

### AI / OCR

* Python
* OpenCV
* OCR Engine
* Machine Learning / AI models
* Natural Language Processing

### Database

* PostgreSQL
* MySQL
* MongoDB

### Storage

* Local storage
* Cloud object storage
* Secure document storage

### DevOps

* Docker
* GitHub Actions
* Linux
* REST API

---

# 🔄 OCR Processing Pipeline

```text
Document / Image
       │
       ▼
Image Upload
       │
       ▼
Quality Validation
       │
       ▼
Image Preprocessing
       │
       ├── Noise Removal
       ├── Grayscale
       ├── Contrast Enhancement
       ├── Rotation Correction
       └── Perspective Correction
       │
       ▼
Text Detection
       │
       ▼
OCR Recognition
       │
       ▼
Text Cleaning
       │
       ▼
AI Processing
       │
       ▼
Structured Information
       │
       ▼
Database / Export
```

---

# 🚀 Getting Started

## Prerequisites

Make sure you have installed:

* Python 3.10+
* Git
* pip
* Docker *(optional)*

---

## 📥 Installation

Clone the repository:

```bash
git clone https://github.com/solvaryn-labs/ai-ocr-scanner.git
```

Navigate to the project:

```bash
cd ai-ocr-scanner
```

Create a virtual environment:

```bash
python -m venv venv
```

Activate the environment.

### Windows

```bash
venv\Scripts\activate
```

### Linux / macOS

```bash
source venv/bin/activate
```

Install dependencies:

```bash
pip install -r requirements.txt
```

---

# ⚙️ Environment Configuration

Create a `.env` file:

```env
APP_ENV=development

DATABASE_URL=your_database_url

OCR_ENGINE=your_ocr_engine

AI_API_KEY=your_api_key

STORAGE_PATH=./storage

MAX_FILE_SIZE=10485760
```

> ⚠️ Never commit `.env` files or API keys to GitHub.

Add the following to `.gitignore`:

```gitignore
.env
venv/
__pycache__/
*.pyc
storage/
uploads/
```

---

# ▶️ Running the API

Start the FastAPI server:

```bash
uvicorn app.main:app --reload
```

The API will be available at:

```text
http://localhost:8000
```

Interactive API documentation:

```text
http://localhost:8000/docs
```

---

# 📡 API Example

### Upload an image

```http
POST /api/v1/ocr
Content-Type: multipart/form-data
```

Example response:

```json
{
  "success": true,
  "document_id": "doc_123456",
  "language": "fr",
  "confidence": 0.96,
  "text": "Example extracted text",
  "processing_time": 1.42
}
```

---

# 📦 Example Structured Output

For an invoice, the AI processing layer can transform OCR text into:

```json
{
  "document_type": "invoice",
  "invoice_number": "INV-2026-001",
  "date": "2026-09-21",
  "customer": "Example Company",
  "total": 12500,
  "currency": "DZD",
  "items": [
    {
      "description": "Software Development",
      "quantity": 1,
      "price": 12500
    }
  ]
}
```

---

# 🔐 Security

Security is an important part of the project.

The application should implement:

* Secure authentication
* JWT-based authorization
* Role-based access control
* File type validation
* File size restrictions
* Input validation
* Secure API endpoints
* Rate limiting
* Secure document storage
* Environment-based secrets
* HTTPS in production

Sensitive documents should never be exposed publicly.

---

# 📁 Project Structure

```text
ai-ocr-scanner/
│
├── app/
│   ├── api/
│   │   ├── routes/
│   │   └── dependencies/
│   │
│   ├── core/
│   │   ├── config.py
│   │   └── security.py
│   │
│   ├── models/
│   │
│   ├── schemas/
│   │
│   ├── services/
│   │   ├── ocr_service.py
│   │   ├── image_service.py
│   │   ├── ai_service.py
│   │   └── document_service.py
│   │
│   └── main.py
│
├── tests/
│
├── uploads/
│
├── storage/
│
├── requirements.txt
├── .env.example
├── .gitignore
├── Dockerfile
├── docker-compose.yml
└── README.md
```

---

# 🧪 Testing

Run the test suite:

```bash
pytest
```

For coverage:

```bash
pytest --cov=app
```

---

# 🐳 Docker

Build the application:

```bash
docker build -t ai-ocr-scanner .
```

Run the container:

```bash
docker run -p 8000:8000 ai-ocr-scanner
```

Or use Docker Compose:

```bash
docker compose up --build
```

---

# 📈 Performance Goals

The project aims to provide:

* Fast document processing
* High OCR accuracy
* Scalable API architecture
* Efficient image preprocessing
* Secure document handling
* Support for large numbers of documents

Actual performance depends on the OCR engine, AI model, hardware, image quality, and document complexity.

---

# 🗺️ Roadmap

### Phase 1 — Core OCR

* [x] Image upload
* [x] OCR processing
* [x] Text extraction
* [ ] PDF processing
* [ ] Multi-page documents

### Phase 2 — AI Processing

* [ ] Document classification
* [ ] Entity extraction
* [ ] Automatic field detection
* [ ] Table extraction
* [ ] Intelligent document summarization

### Phase 3 — Mobile

* [ ] Flutter application
* [ ] Camera scanning
* [ ] Real-time document detection
* [ ] Offline OCR

### Phase 4 — Enterprise

* [ ] Multi-user management
* [ ] Role-based permissions
* [ ] Cloud storage
* [ ] Advanced analytics
* [ ] Audit logs
* [ ] Enterprise API

---

# 🌍 Future Improvements

Possible future integrations include:

* AI-powered document classification
* Handwriting recognition
* Voice reading of extracted text
* Automatic translation
* Intelligent search
* RAG-based document assistant
* Cloud deployment
* Mobile SDK
* Enterprise document workflows

---

# 👥 Team

### Solvaryn Labs

**Roqia Benaboud**
AI & Software Development

**Abderrahim Salem**
Information Systems & Software Development

---

# 📬 Contact

📧 **Email:** [solvarynlabs@gmail.com](mailto:solvarynlabs@gmail.com)

For collaboration, software development, AI solutions, IoT projects, or student/PFE projects, contact the Solvaryn Labs team.

---

# 📄 License

This project is distributed under the license specified in the repository.

If this repository contains proprietary components, please refer to the project's licensing terms before using, modifying, or redistributing the code.

---

## ⭐ Support

If you find this project useful:

⭐ Star the repository
🍴 Fork the project
🐛 Report issues
💡 Suggest improvements
🤝 Contribute to the project

---

**Built with ❤️ by Solvaryn Labs** et BENABOUD ROQIA 
