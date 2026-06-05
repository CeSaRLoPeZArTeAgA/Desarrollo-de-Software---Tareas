FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN chmod +x scripts/*.sh && ./scripts/compile.sh
EXPOSE 8080
CMD ["java", "-cp", "build/classes", "pe.edu.uni.fc.cc.citizenvoice.app.CitizenVoiceServer"]
