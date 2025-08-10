package com.example.com.venom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

@SpringBootApplication
public class VenomApplication {

    public static void main(String[] args) {
        SpringApplication.run(VenomApplication.class, args);
    }

    

    @Component
public static class ServerInfoPrinter implements ApplicationListener<WebServerInitializedEvent> {
    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        try {
            int port = event.getWebServer().getPort();
            String ip = getLocalNetworkIp();

            System.out.println("\n✅ Сервер запущен!");
            System.out.println("Вызов скрипта...");

            // 👉 Вызов Python-скрипта
            runPythonScript();

            
        } catch (Exception e) {
            System.err.println("❌ Ошибка при получении информации о сервере: " + e.getMessage());
        }
    }

    private void runPythonScript() {
        try {
            // Путь к твоему скрипту (если он в одной папке с jar)
            ProcessBuilder pb = new ProcessBuilder("python", "update_ngrok_gist.py");

            // 👉 Укажи путь к папке, где лежит скрипт
            pb.directory(new File("src/main/resources/scripts")); // Измени на свой путь

            // Наследовать вывод в консоль
            pb.inheritIO();

            // Запуск
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("✅ Python-скрипт выполнен успешно.");
            } else {
                System.err.println("❌ Python-скрипт завершился с ошибкой. Код: " + exitCode);
            }

        } catch (Exception e) {
            System.err.println("❌ Ошибка при запуске Python-скрипта: " + e.getMessage());
        }
    }

    private String getLocalNetworkIp() throws Exception {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            if (iface.isLoopback() || !iface.isUp()) continue;

            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr instanceof Inet4Address) {
                    return addr.getHostAddress();
                }
            }
        }
        return "127.0.0.1";
    }
}

}