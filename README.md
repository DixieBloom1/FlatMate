# 📦 FlatMate — Android App for Roommate Management

![Platform](https://img.shields.io/badge/platform-Android-green)
![License](https://img.shields.io/badge/license-Academic--Project-lightgrey)
![Status](https://img.shields.io/badge/status-Completed-brightgreen)
![Made with Kotlin](https://img.shields.io/badge/made%20with-Kotlin-orange)

> FlatMate is a mobile application that helps students and young adults find roommates and manage shared expenses, profiles, and daily responsibilities in a shared living environment.

---

## 📸 Screenshots

| Login Screen | Home Screen | Roommate Ads | Expenses |
|--------------|-------------|---------------|----------|
| ![login](docs/screenshots/login.png) | ![home](docs/screenshots/home.png) | ![ads](docs/screenshots/ads.png) | ![expenses](docs/screenshots/expenses.png) |

---

## 🚀 Features

- 🔐 **Authentication** – Register & log in with Firebase Authentication  
- 👤 **Profile Management** – Edit personal information, update password  
- 📢 **Roommate Ads** – Create and browse ads, contact potential roommates  
- 🧑‍🤝‍🧑 **Roommate Linking** – Connect with other users via a unique ID  
- 💸 **Expense Tracking** – Add and split shared costs with your roommates  
- 🔔 **Notifications** – Enable or disable push notifications  
- 🌐 **Cloud Storage** – Real-time sync using Firebase Firestore  

---

## 🛠️ Tech Stack

- **Kotlin** – Primary language for development  
- **XML** – UI layout and screen structure  
- **Firebase Firestore** – NoSQL cloud database  
- **Firebase Authentication** – User account management  
- **Jetpack Libraries** – Navigation, ViewModel, LiveData (MVVM architecture)  
- **Android Studio** – Development environment  

---

## 🧱 Database Structure

### 🔹 `/userProfiles`
Stores user information and associations.

| Field             | Type           | Description                          |
|------------------|----------------|--------------------------------------|
| name, surname     | `String`       | User’s full name                     |
| birthYear         | `String`       | Year of birth                        |
| faculty           | `String`       | University faculty                   |
| roommateIds       | `Array<String>`| Linked roommate IDs                  |
| expensesIds       | `Array<String>`| Expenses user is involved in         |
| currentRoomId     | `String`       | Identifier for shared room/group     |

---

### 🔹 `/ads`
Holds public roommate search ads.

| Field     | Type     | Description                          |
|-----------|----------|--------------------------------------|
| title     | `String` | Ad title                             |
| description | `String` | Description text                     |
| location  | `String` | City or neighborhood                 |
| contact   | `String` | Phone number                         |

---

### 🔹 `/expenses`
Tracks shared financial obligations.

| Field           | Type                   | Description                           |
|----------------|------------------------|---------------------------------------|
| title           | `String`              | Expense name (e.g., rent, Wi-Fi)      |
| totalAmount     | `Number`              | Total cost                            |
| creatorId       | `String`              | User who added the expense            |
| involvedUsers   | `Array<String>`       | List of user IDs involved             |
| amountsPerUser  | `Map<String, Number>` | Split amounts per user                |

---

## 📲 Getting Started

### 🔧 Prerequisites
- Android Studio Flamingo or newer
- Kotlin 1.9+
- Firebase project (with Firestore & Authentication enabled)

### ⚙️ Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/flatmate.git
   ```

2. **Open in Android Studio**  
   File > Open > `flatmate` folder

3. **Add Firebase configuration**  
   - Place your `google-services.json` in the `/app` directory

4. **Build and run** on an emulator or physical device

---

## 🎯 Project Structure

```
📂 FlatMate/
 ┣ 📁 activities/
 ┃ ┣ LoginActivity.kt
 ┃ ┣ RegisterActivity.kt
 ┃ ┣ HomeActivity.kt
 ┃ ┣ AddAdActivity.kt
 ┃ ┣ AddExpenseActivity.kt
 ┃ ┗ ...
 ┣ 📁 layouts/
 ┃ ┣ activity_login.xml
 ┃ ┣ activity_home.xml
 ┃ ┗ ...
 ┣ 📁 model/
 ┃ ┗ User.kt, Expense.kt, Ad.kt
 ┣ 📄 google-services.json
 ┗ 📄 README.md
```

---

## 📚 References

- [Firebase Documentation](https://firebase.google.com/docs)
- [Android Developer Guide](https://developer.android.com/docs)
- [Kotlin Language Reference](https://kotlinlang.org/docs/home.html)

---

## 👨‍🎓 Academic Info

Developed by **Sergej Karas**  
Course: *Mobile App Development*  
Faculty: FERIT Osijek  
Supervisor: Prof. Josip Balen  
Assistants: Miljenko Švarcmajer, Antonio Antunović

---

## 📄 License

This project was developed as part of an academic assignment.  
For educational purposes only — not intended for commercial use.
