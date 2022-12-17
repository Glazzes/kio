FROM amazoncorretto:17
RUN apt-get update --fix-missing
RUN DEBIAN_FRONTEND="noninteractive" add-apt-repository ppa:chris-needham/ppa -y
RUN DEBIAN_FRONTEND="noninteractive" sudo apt-get update -y
RUN DEBIAN_FRONTEND="noninteractive" sudo apt-get install audiowaveform -y
WORKDIR app
COPY build/libs/kio-0.0.1-SNAPSHOT.jar .
CMD ["java", "-jar", "qr-login-0.0.1-SNAPSHOT.jar"]