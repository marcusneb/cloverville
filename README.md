# cloverville

## Documentation for the project: 
[Project and Process report] (Project Report_merged.pdf)

## Overview
Cloverville is a sustainable village management system designed to optimize 
how administrators track resident activities, trade offers, and community 
operations. The project replaces manual record-keeping with a structured, 
scalable backend system.

## My Role – Backend Developer & System Architect
- Designed the complete system architecture including UML diagrams and use cases
- Built the backend business logic using a layered architecture to support team collaboration
- Applied OOP principles to model and manage sustainable village activities

  ## Project Structure
- `src/` – Java source code
- `cloverville_website/` – Frontend website


## Getting Started

### Prerequisites
- Java 17 or higher
- Maven (included via `mvnw`)

### Installation

1. Clone the repository
   ```bash
   git clone https://github.com/marcusneb/cloverville.git
   cd cloverville
   ```

2. Run the application
   ```bash
   ./mvnw spring-boot:run
   ```
   Or open the project in IntelliJ IDEA and run it directly.

3. Open the website
   - Navigate to `cloverville_website/index.html` in your browser

### Data
All application data is stored locally in `cloverville_export.json`.
No database setup required.
