FROM debian:bookworm-slim

ENV DEBIAN_FRONTEND=interactive

RUN apt-get update && apt-get install -y \
    wget xz-utils python3 python3-pip \
    # Qt + X11 + GL deps Calibre requires
    libxcb-cursor0 \
    libopengl0 \
    libgl1 \
    libegl1 \
    libxkbcommon0 \
    libxi6 \
    libxrandr2 \
    libxrender1 \
    libxext6 \
    libxfixes3 \
    libx11-6 \
    libxau6 \
    libxdmcp6 \
    libfontconfig1 \
    libfreetype6 \
    libglib2.0-0 \
    libgstreamer1.0-0 \
    libgstreamer-plugins-base1.0-0 \
    libharfbuzz0b \
    libjpeg62-turbo \
    libpng16-16 \
    libqt5gui5 \
    libqt5widgets5 \
    libqt5core5a \
    libsqlite3-0 \
    libnss3 \
    libnspr4 \
    zlib1g \
    libxml2 \
    libxslt1.1 \
    liblcms2-2 \
    libtiff6 \
    libwebp7 \
    libopenjp2-7 \
    libjpeg-dev \
    fontconfig \
    fonts-dejavu-core \
    ca-certificates \
    less \
    nano \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Instalar Calibre (binário oficial)
RUN wget -nv -O- https://download.calibre-ebook.com/linux-installer.sh | sh /dev/stdin

# Pastas padrão para os livros
RUN mkdir -p /origin_books /converted_books

# =========================
#  ADIÇÕES PARA SSH
# =========================

# Instalar servidor SSH
RUN apt-get update && apt-get install -y openssh-server \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Diretório de runtime do sshd
RUN mkdir -p /var/run/sshd
# Criar usuário e senha  
RUN useradd -m -s /bin/bash pedrolucassouzacarneiro \  
    && echo 'pedrolucassouzacarneiro:asaventurasdetimtimeseusamigos' | chpasswd  
  
# Adicionar chave SSH pública  
RUN mkdir -p /home/pedrolucassouzacarneiro/.ssh \  
    && echo "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIDjibN2MUn+aqwlfl1XfHFJ+lYL58qt4/tQbKq4TqWSU pedro@pedro" > /home/pedrolucassouzacarneiro/.ssh/authorized_keys \  
    && chown -R pedrolucassouzacarneiro:pedrolucassouzacarneiro /home/pedrolucassouzacarneiro/.ssh \  
    && chmod 700 /home/pedrolucassouzacarneiro/.ssh \  
    && chmod 600 /home/pedrolucassouzacarneiro/.ssh/authorized_keys

# Garantir que autenticação por senha esteja habilitada
RUN sed -i 's/#PasswordAuthentication yes/PasswordAuthentication yes/' /etc/ssh/sshd_config

# (Opcional, mas mais seguro) impedir login de root por SSH
RUN sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin no/' /etc/ssh/sshd_config

# Expor porta SSH
EXPOSE 22

# Criar o arquivo entrypoint.sh diretamente no Dockerfile
RUN echo '#!/bin/bash' > /entrypoint.sh \
    && echo 'set -e' >> /entrypoint.sh \
    && echo '' >> /entrypoint.sh \
    && echo '# Garantir diretório do sshd' >> /entrypoint.sh \
    && echo 'mkdir -p /var/run/sshd' >> /entrypoint.sh \
    && echo '' >> /entrypoint.sh \
    && echo '# Iniciar sshd em background' >> /entrypoint.sh \
    && echo '/usr/sbin/sshd -D &' >> /entrypoint.sh \
    && echo '' >> /entrypoint.sh \
    && echo '# Executar o comando padrão do container (CMD do Dockerfile)' >> /entrypoint.sh \
    && echo 'exec "$@"' >> /entrypoint.sh \
    && chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]

# Manter o container vivo, pronto para receber 'docker exec'
CMD ["/bin/bash"]