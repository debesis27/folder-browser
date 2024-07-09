# File Browser

Folder Browser is a web application built with Java, Spring Boot, and Thymeleaf that allows users to view and manage their local directories through a web browser.
## Table of Contents

- [Description](#description)
- [Features](#features)
- [Screenshots](#screenshots)
- [Tech Stack](#tech-stack)
- [Installation](#installation)
## Features

- **View Directory Structure**: Easily browse the entire directory structure of the configured root directory.
- **Create Folders**: Create new folders within the directory.
- **Upload Files**: Upload multiple files simultaneously.
- **Delete Files/Folders**: Delete multiple files or folders at once.
- **Move and Copy**: Move or copy files and folders to different locations.
- **Rename**: Rename files and folders.
- **View File Details**: Display file details including size, number of folders, and files within each folder.
- **Open Files**: Open files directly in the web browser with support for different file types.
- **Unique Icons**: Display unique icons for different file types (e.g., PDF, Word, etc.).
- **Search**: Search for files and folders.
- **Breadcrumb Navigation**: Breadcrumbs are displayed at the top for easy navigation.
## Screenshots

**Home Page**

<img src="/ReadMe-Images/home.jpg" width="700" height="400">

\
**File Operations**

<img src="/ReadMe-Images/file-operations.jpg" width="700" height="400"> 

\
**File Icons**

<img src="/ReadMe-Images/file-icons.jpg" width="700" height="400">

\
**Search**

<img src="/ReadMe-Images/search.jpg" width="700" height="400"> 

## Tech Stack

- **Java**
- **Spring Boot**
- **Thymeleaf**
- **HTML/CSS/JavaScript**
## Run Locally

**Clone the repository**:

```bash
git clone https://github.com/debesis27/folder-browser.git
cd folder-browser
```

**Set the root directory**:

Open the `application.properties` file and set the root directory:
```properties
filebrowser.scan.directory=/path/to/your/directory (eg. D:)
```

**Build and run the application**:

```bash
./mvnw spring-boot:run
```

**Access the application**:

Open your web browser and go to http://localhost:4000.