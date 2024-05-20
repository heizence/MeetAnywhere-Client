# MeetAnywhere-Client

화상회의 서비스 Zoom 의 Android 앱을 비슷하게 만들어 보는 클론 코딩 프로젝트. Zoom 의 모든 기능을 다 구현하지는 않고 핵심적인 기능 위주로 구현하였다.

**본 Repository 는 Client side repository 임.** 

[Server side repository 바로가기](https://github.com/heizence/MeetAnywhere-Server)

### Built with

* Java
* Android
* WebRTC

프로젝트에 대한 상세 설명은 다음 링크를 참고할 것.

[heizence.devlog - Zoom Android 앱 클론코딩](https://heizence.github.io/posts/androidProject/)

## Getting started

### Prerequisite

- JDK version >= 11.0.0
- Android API level >= 33
- WebRTC ICE 서버(STUN, TURN)

ICE 서버는 google STUN 과 coTURN 을 사용하는 것을 권장함.

[google STUN 서버 리스트](https://gist.github.com/zziuni/3741933)   
[coTURN 서버 repository](https://github.com/coturn/coturn?tab=readme-ov-file)   
[coTURN 서버 config 설정 안내](https://github.com/coturn/coturn/blob/master/examples/etc/turnserver.conf)  
[ICE 서버 테스트](https://webrtc.github.io/samples/src/content/peerconnection/trickle-ice/)

### Installation

Repository 클론

```
git clone https://github.com/heizence/MeetAnywhere-Client
```

클론 후 Android Studio 에서 프로젝트 import 하기

root 디렉토리에 있는 local.properties 파일에 다음 값들 추가하기

```
# 차례대로,
# 테스트 시 사용할 로컬 테스트 서버 ip
# 배포 환경에서 사용할 실서버 ip
# stun 서버 url
# turn 서버 url
# turn 서버 사용 시 등록한 이름
# turn 서버 사용 시 등록한 암호

LOCAL_IP="x.x.x.x"
PRODUCTION_IP="x.x.x.x"
STUN_URL="stun:stun.x.x.x:xxxx"  
TURN_URL="turn:x.x.x.x:xxxx"  
TURN_username="username"
TURN_password="password"
```

추가 후, build.gradle(:app) 파일에 설정 추가하기

```
...
def localProperties = new Properties()
localProperties.load(new FileInputStream(rootProject.file("local.properties")))

android {
  ...
  defaultConfig {
    ...
    buildConfigField("String", "LOCAL_IP", localProperties['LOCAL_IP'])
    buildConfigField("String", "PRODUCTION_IP", localProperties['PRODUCTION_IP'])
    buildConfigField("String", "STUN_URL", localProperties['STUN_URL'])
    buildConfigField("String", "TURN_URL", localProperties['TURN_URL'])
    buildConfigField("String", "TURN_username", localProperties['TURN_username'])
    buildConfigField("String", "TURN_password", localProperties['TURN_password'])
  }
}
```

빌드 유형에 따라 세분화된 설정이 필요하면 BuildTypes 영역 내 debug, release, staging 등 각 타입에서 타입에 변수를 다르게 설정해 줄 수 있음.

```
// 예시
android {
  ...
  defaultConfig {
    ...
    buildConfigField("String", "STUN_URL", localProperties['STUN_URL'])
    buildConfigField("String", "TURN_URL", localProperties['TURN_URL'])
    buildConfigField("String", "TURN_username", localProperties['TURN_username'])
    buildConfigField("String", "TURN_password", localProperties['TURN_password'])
  }
  BuildTypes {
    debug {
      buildConfigField("String", "LOCAL_IP", localProperties['LOCAL_IP'])
    }
    release {
      buildConfigField("String", "PRODUCTION_IP", localProperties['PRODUCTION_IP'])
    }
  }
}
```
