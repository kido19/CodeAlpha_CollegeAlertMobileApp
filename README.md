# 📱 College Alert App

A real-time notification system for college students and administrators. Built with Kotlin and Firebase.

## ✨ Features

### For Students
- 📝 Receive real-time alerts for exams, seminars, holidays, notices, and emergencies
- 🔔 Push notifications for new alerts
- 📊 Track read/unread status
- 🌙 Dark mode support
- 🔐 Secure authentication

### For Administrators
- 📢 Create and broadcast alerts to all students
- 🎯 Categorize alerts by type (Exam, Seminar, Holiday, Notice, Urgent)
- ⚡ Set priority levels (High, Medium, Low)
- 📋 View and manage alert history
- 🗑️ Delete past alerts

## 🛠️ Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| Local Database | Room |
| Backend | Firebase (Auth, Realtime DB, FCM) |
| UI | Material Design 3, ViewBinding |
| Authentication | Firebase Auth |
| Real-time Updates | Firebase Realtime Database |


## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17
- Firebase account (free tier works fine)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/kido19/CodeAlpha_CollegeAlertMobileApp.git
2. **Open in Android Studio**
    ````bash
    File → Open → Select the cloned folder
3. **Set up Firebase**
    ````bash
    Go to Firebase Console

    Create new project or use existing

    Add Android app with package name: com.example.collagealert

    Download google-services.json

    Place it in app/ folder

4. **Enable Firebase Services**
    ````bash
    Authentication → Sign-in method → Enable Email/Password

    Realtime Database → Create database in test mode
   
5. **Build and run**
    ````bash
    Click the Run button or use ./gradlew build


## 📱 How to Use

### Student Account

Email: kid@gmail.com

Password: 12345678

Or you can register
## Admin Account

Email: admin@gmail.com  
Password: 12345678


## 🔧 Firebase Configuration
### Realtime Database Rules

    json 
    {
        "rules": {
        ".read": "auth != null",
        ".write": "auth != null",
        "users": {
        "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
    }
    },
    "notices": {
    ".read": "auth != null",
    ".write": "auth != null"
    }
    }
    }
## 🎯 Future Enhancements
    * Department-specific alerts

    * Push notification scheduling

    *Image/file attachments

    *Analytics dashboard for admins

    *Comment section on alerts

    *Email notifications

    *Calendar integration

### 🤝 Contributing
    Contributions are welcome! Please follow these steps:
# 📧 Contact
    Kidist Kinfe - kidstekinfe@gmail.com
    
    Project Link: https://github.com/kido19/CodeAlpha_CollegeAlertMobileApp.git

## 🙏 Acknowledgments
    Firebase Documentation
    
    Android Developers
    
    Material Design

