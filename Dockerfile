FROM openjdk:slim AS builder

RUN apt-get update
RUN apt-get install gnupg apt-transport-https -y

RUN echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
RUN apt-get update
RUN apt-get install sbt -y

COPY build.sbt /source/build.sbt
COPY app /source/app
COPY conf /source/conf
COPY project /source/project
COPY public /source/public
COPY test /source/test

WORKDIR /source 
RUN sbt stage


FROM openjdk:slim
COPY --from=builder /source/target/universal/stage/ /opt/
CMD ["/opt/bin/poke-api"]