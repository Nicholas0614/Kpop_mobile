# 🎧 K-Pop Mobile

A K-Pop merchandise mobile application built with **Kotlin and Android Studio**.

This project was created to explore Android development while building something around one of my biggest interests — K-Pop. 🎶

## ✨ Features

### 👤 User

* User registration and login
* User profile
* Local user data storage

### 🛍️ Products

* Browse K-Pop products
* View product details
* Product categories
* Product pricing and stock information

### 🛒 Shopping Cart

* Add products to cart
* Update product quantity
* Remove products from cart
* View cart total

### ❤️ Wishlist

* Add products to wishlist
* Remove products from wishlist
* View saved products
* Display product name and price

### 📦 Orders

* Place orders
* View order history
* Store order information locally

### ⭐ Reviews

* Add product reviews
* Rating system
* View reviews for products

## 🛠️ Tech Stack

### Mobile Development

* **Kotlin**
* **Android Studio**
* **Android SDK**
* **XML Layouts**

### Database

* **Room Database**
* SQLite

### Architecture & Libraries

* Room
* ViewModel
* LiveData
* RecyclerView
* Android Navigation
* Kotlin Coroutines

## 🗄️ Local Database

The application uses **Room Database** for local data persistence.

Main entities include:

* User
* Product
* Cart
* Order
* Review

Room allows the application to store and manage user, product, cart, order, and review information locally on the device.

## 📱 Main App Flow

```text
Login / Register
       │
       ▼
    Home Page
       │
       ├── Products
       │     └── Product Details
       │             ├── Add to Cart
       │             └── Add to Wishlist
       │
       ├── Wishlist
       │
       ├── Cart
       │     └── Checkout
       │             └── Order
       │
       └── Profile
              └── Order History
```

## 📂 Project Structure

```text
Kpop_mobile/
│
├── app/
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/example/kpop/
│           │
│           ├── res/
│           │   ├── drawable/
│           │   ├── layout/
│           │   ├── mipmap/
│           │   └── values/
│           │
│           └── AndroidManifest.xml
│
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## 🚀 Getting Started

### Prerequisites

Make sure you have:

* Android Studio
* Android SDK
* JDK
* An Android emulator or physical Android device

### 1. Clone the repository

```bash
git clone https://github.com/Nicholas0614/Kpop_mobile.git
cd Kpop_mobile
```

### 2. Open the project

Open the project using **Android Studio**.

Allow Gradle to sync and download the required dependencies.

### 3. Run the application

Select an Android emulator or connect an Android device, then click **Run ▶** in Android Studio.

## 💡 What I Learned

Building this application helped me gain practical experience in:

* Android application development
* Kotlin programming
* Android Studio
* Room Database
* SQLite
* CRUD operations
* RecyclerView
* Android navigation
* Local data persistence
* Managing application state
* Building reusable UI components
* Connecting different parts of an Android application

## 🎶 Why I Built This

I'm a K-Pop fan, so naturally I ended up making a K-Pop app. 😂

This project gave me a chance to combine something I enjoy with Android development and learn how a mobile e-commerce application works from the inside.

**K-Pop + Kotlin = another project I probably didn't need to make, but definitely wanted to.** 🎧💻

## 👨‍💻 Author

**Nicholas**

GitHub: [Nicholas0614](https://github.com/Nicholas0614)

---

⭐ If you like K-Pop, Android development, or both, feel free to check out the project!
