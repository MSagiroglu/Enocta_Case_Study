# JDK 17 ve Maven iceren temel imaj
FROM maven:3.9.5-eclipse-temurin-17

# Gerekli araclari ve bagimliliklari yukle
RUN apt-get update && apt-get install -y wget gnupg2 curl unzip

# Google Chrome'u yukle
RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && sh -c 'echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list' \
    && apt-get update && apt-get install -y google-chrome-stable

# Firefox'u yukle
RUN apt-get install -y firefox

# Node.js ve npm yukle (Mock server icin)
RUN curl -fsSL https://deb.nodesource.com/setup_18.x | bash - \
    && apt-get install -y nodejs

# Calisma dizinini ayarla
WORKDIR /app

# Pom ve bagimliliklari kopyala (önbelleklemek icin)
COPY pom.xml .
RUN mvn dependency:resolve

# Proje dosyalarini kopyala
COPY src ./src
COPY mock-server ./mock-server

# Mock server bagimliliklarini kur
RUN npm install --prefix mock-server

# Default cevre degiskenleri
ENV BROWSER=chrome
ENV TAGS=""
ENV CI=true

# Test komutu (browser ve tag parametrelerini alir)
CMD ["sh", "-c", "mvn clean verify -Dbrowser=${BROWSER} -Dcucumber.filter.tags=\"${TAGS}\""]
