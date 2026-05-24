# Digital-Footprint-and-Privacy-Risk-Tracker-
AI-powered digital footprint and privacy risk checker using Spring Boot, MySQL, Naive Bayes classification, and a browser extension.


# Digital Footprint & Privacy Risk Checker

## Overview
Digital Footprint & Privacy Risk Checker is a web-based privacy analysis system that evaluates a user's browsing patterns and generates a privacy risk score. The project uses a browser extension to collect browsing-related data and a Spring Boot backend to analyze digital footprint risk using classification and scoring techniques.

## Features
- Browser activity analysis
- Privacy risk score generation
- Naive Bayes-based website classification
- Category-wise browsing pattern analysis
- Shannon Entropy-based risk calculation
- Time-weighted risk scoring
- Session history storage
- Interactive dashboard interface
- Browser extension integration

## Tech Stack
- Java
- Spring Boot
- Spring Data JPA
- MySQL
- Maven
- HTML
- CSS
- JavaScript
- Chrome Extension Manifest V3

## Project Modules
- **Backend API:** Handles browser scan requests and risk analysis
- **Risk Scoring Service:** Calculates privacy risk based on browsing behavior
- **Naive Bayes Classifier:** Classifies websites into risk categories
- **Database Layer:** Stores browser session history using MySQL
- **Frontend Dashboard:** Displays analysis results and risk insights
- **Browser Extension:** Collects browsing data and connects with the dashboard

## Risk Categories
The system classifies visited websites into categories such as:
- Social Media
- Shopping
- Financial
- Privacy Tools
- Data Broker
- Adult
- Gambling
- Neutral

## How It Works
1. The browser extension collects browsing-related information.
2. The data is sent to the Spring Boot backend.
3. Websites are classified using a Naive Bayes classifier.
4. The system calculates category distribution and entropy.
5. A final privacy risk score is generated.
6. Results are displayed on the dashboard and stored in MySQL.

## Learning Outcomes
Built a full-stack privacy risk analysis system
Implemented REST APIs using Spring Boot
Used MySQL for storing user browser session data
Applied Naive Bayes classification for website categorization
Integrated a browser extension with a backend service
Designed a dashboard for displaying privacy insights

## Future Scope
Add user authentication
Improve ML model accuracy with larger datasets
Add real-time alerts for risky browsing behavior
Deploy the backend and database on cloud platforms
Add support for multiple browsers

## Author
Abhir Bengali
