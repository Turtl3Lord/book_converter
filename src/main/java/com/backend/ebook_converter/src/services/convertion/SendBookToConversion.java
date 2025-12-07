package com.backend.ebook_converter.src.services.convertion;

import com.backend.ebook_converter.src.Responses.SendToConversionRespondeModel;
import com.backend.ebook_converter.src.interfaces.ISendBookToConversion;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.xfer.FileSystemFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

@Service
public class SendBookToConversion implements ISendBookToConversion {

    @Value("${ssh.host}")
    private String sshHost;

    @Value("${ssh.port}")
    private int sshPort;

    @Value("${ssh.user}")
    private String sshUser;

    @Value("${ssh.private-key-path}")
    private String sshPrivateKeyPath;

    @Value("${ssh.origin-dir:/origin_books}")
    private String originDir;

    @Value("${ssh.converted-dir:/converted_books}")
    private String convertedDir;

    @Value("${ssh.private-key-passphrase:}")
    private String sshPrivateKeyPass;

    @Override
    public SendToConversionRespondeModel sendBookToConversion(byte[] bookData, String originalFormat, String targetFormat) {
        // Retorno: conteúdo do livro convertido em Base64
        File localTempUpload = null;
        File localTempConverted = null;
        SSHClient ssh = new SSHClient();

        String fileId = UUID.randomUUID().toString();
        String originExt = normalizeExtension(originalFormat);
        String targetExt = normalizeExtension(targetFormat);

        String remoteOriginPath = originDir + "/" + fileId + originExt;
        String remoteOutputPath = convertedDir + "/" + fileId + targetExt;

        try {
            // 1) Criar arquivo temporário local com o conteúdo recebido
            localTempUpload = File.createTempFile("upload-", originExt);
            Files.write(localTempUpload.toPath(), bookData);

            // 2) Conectar no servidor SSH
            ssh.addHostKeyVerifier(new PromiscuousVerifier()); // cuidado em produção
            ssh.connect(sshHost, sshPort);
            KeyProvider kp = ssh.loadKeys(
                    sshPrivateKeyPath,
                    sshPrivateKeyPass.isBlank() ? null : sshPrivateKeyPass.toCharArray()
            );
            ssh.authPublickey(sshUser, kp);

            // 3) Enviar arquivo para /origin_books no servidor
            ssh.newSCPFileTransfer().upload(new FileSystemFile(localTempUpload), remoteOriginPath);

            // 4) Executar ebook-convert no servidor
            String command = String.format("ebook-convert %s %s", remoteOriginPath, remoteOutputPath);
            try (Session session = ssh.startSession()) {
                Session.Command cmd = session.exec(command);

                // Capturar stdout/stderr (opcional, útil para debug)
                ByteArrayOutputStream stdout = new ByteArrayOutputStream();
                ByteArrayOutputStream stderr = new ByteArrayOutputStream();
                cmd.getInputStream().transferTo(stdout);
                cmd.getErrorStream().transferTo(stderr);

                cmd.join(); // aguarda comando terminar

                if (cmd.getExitStatus() != 0) {
                    throw new RuntimeException("Erro na conversão remota. stdout=" +
                            stdout + " stderr=" + stderr);
                }
            }

            SendToConversionRespondeModel responseModel = new SendToConversionRespondeModel(fileId);

            return responseModel ;

        } catch (Exception e) {
            throw new RuntimeException("Falha ao enviar livro para conversão via SSH", e);
        }
    }

    private String normalizeExtension(String extOrFormat) {
        if (extOrFormat == null || extOrFormat.isBlank()) {
            return "";
        }
        String e = extOrFormat.trim();
        if (!e.startsWith(".")) {
            e = "." + e;
        }
        return e.toLowerCase();
    }
}