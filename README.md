<h1 align="center">MeetAnywhere-Client</h1>
<div align="center">

  <b>Zoom 클론 코딩 프로젝트</b>

  <br/>
  
<img width="1073" alt="meetanywhere_demo" src="https://github.com/heizence/MeetAnywhere-Client/assets/47074893/f168a2cb-e3ac-4ecd-85f8-bd19d785ab4c">

<span>Loved the project? Please consider [donating](https://paypal.me/abhisheknaiidu) to help it improve!</span>

</div>

## Contents
- [About](#about)
  - [Built with](#built-with)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Usage](#usage)

## About

화상회의 서비스 Zoom 의 Android 앱을 비슷하게 만들어 보는 클론 코딩 프로젝트입니다. Zoom 의 모든 기능을 다 구현하지는 않고 핵심적인 기능 위주로 구현하였습니다.

본 repository 는 서비스의 Android 앱의 소스 코드를 포함하고 있습니다. 라이브 스트리밍을 위해 WebRTC 라이브러리를 사용하였고, Mesh(p2p) 방식에 따라 설계하였습니다.

WebRTC Mesh 방식의 특성상, 접속자 수가 늘어나면 각 클라이언트가 감당해야 부하가 급격히 증가하므로 다수의 접속자를 감당하기에는 부적절합니다. 본 클라이언트는 회의당 4명 이하의 소규모 인원을 수용하는 데 적합합니다.


### Built with

* ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
* ![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
* <img src="https://webrtc.github.io/webrtc-org/assets/images/webrtc-logo-horiz-retro-300x60.png" height="20px" />

<br/>

**\*본 Repository 는 Client side repository 입니다.**

Server side repository : [https://github.com/heizence/MeetAnywhere-Server](https://github.com/heizence/MeetAnywhere-Server)

## Getting started

### Prerequisites

This is an example of how to list things you need to use the software and how to install them.
* npm
  ```sh
  npm install npm@latest -g
  ```

### Installation

_Below is an example of how you can instruct your audience on installing and setting up your app. This template doesn't rely on any external dependencies or services._

1. Get a free API Key at [https://example.com](https://example.com)
2. Clone the repo
   ```sh
   git clone https://github.com/your_username_/Project-Name.git
   ```
3. Install NPM packages
   ```sh
   npm install
   ```
4. Enter your API in `config.js`
   ```js
   const API_KEY = 'ENTER YOUR API';
   ```

## Usage

Use this space to show useful examples of how a project can be used. Additional screenshots, code examples and demos work well in this space. You may also link to more resources.

