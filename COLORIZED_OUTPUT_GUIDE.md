# 🎨 Colorized Output Guide for Maven Spring Boot

## Overview
This guide shows you how to get beautiful colorized output when running `mvn spring-boot:run` for better log readability and debugging.

## 🚀 Quick Commands (Choose Any)

### **Method 1: Simple Command Line Option**
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments='-Dspring.output.ansi.enabled=always'
```

### **Method 2: Enhanced Color with Maven**
```bash
mvn -Djansi.force=true spring-boot:run -Dspring-boot.run.jvmArguments='-Dspring.output.ansi.enabled=always'
```

### **Method 3: Using Color Profile** (Recommended)
```bash
mvn spring-boot:run -Pcolor
```

### **Method 4: Environment Variable**
```bash
export SPRING_OUTPUT_ANSI_ENABLED=always
mvn spring-boot:run
```

## 🎯 What Each Method Does

### **Configuration Method 1: Application Properties**
Added to `application.yml`:
```yaml
spring:
  output:
    ansi:
      enabled: always
```

### **Configuration Method 2: Logback with Colors**
Created `logback-spring.xml` with color patterns:
```xml
<pattern>
    %clr(%d{yyyy-MM-dd HH:mm:ss}){faint} 
    %clr([%thread]){magenta} 
    %clr(%-5level){highlight} 
    %clr(%logger{36}){cyan} - 
    %clr(%msg%n){green}
</pattern>
```

### **Configuration Method 3: Maven Profile**
Added to `pom.xml`:
```xml
<profile>
    <id>color</id>
    <properties>
        <spring.output.ansi.enabled>always</spring.output.ansi.enabled>
    </properties>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <jvmArguments>-Dspring.output.ansi.enabled=always -Djansi.force=true</jvmArguments>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>
```

## 🌈 Color Meanings in Logs

- **🔴 ERROR**: Red text for error messages
- **🟡 WARN**: Yellow text for warnings
- **🔵 INFO**: Blue text for information
- **🟢 DEBUG**: Green text for debug messages
- **🟣 TRACE**: Purple text for trace messages
- **⚪ Timestamps**: Faint gray
- **🟦 Thread Names**: Magenta
- **🟩 Logger Names**: Cyan

## 📊 Color Output Examples

When you run with colors enabled, you'll see:

```
2025-07-22 19:59:45 [main] INFO  c.d.s.SpringbootGrpcRestDemoApplication - Starting SpringbootGrpcRestDemoApplication
2025-07-22 19:59:45 [main] DEBUG o.s.security.web.FilterChainProxy - Securing POST /api/users
2025-07-22 19:59:45 [main] ERROR o.s.web.servlet.DispatcherServlet - Context initialization failed
```

Will appear as:
- **Timestamp**: Light gray
- **Thread name**: Purple/Magenta
- **Log level**: 
  - INFO: Blue
  - DEBUG: Green  
  - ERROR: Red
- **Logger name**: Cyan
- **Message**: Default color or green

## 🛠️ Troubleshooting

### **If colors don't appear:**

1. **Check terminal support:**
   ```bash
   echo $TERM
   # Should show: xterm-256color or similar
   ```

2. **Force colors:**
   ```bash
   mvn -Djansi.force=true spring-boot:run -Dspring-boot.run.jvmArguments='-Dspring.output.ansi.enabled=always'
   ```

3. **Test ANSI support:**
   ```bash
   echo -e "\033[31mThis should be red\033[0m"
   ```

### **macOS/Linux specific:**
```bash
# Set terminal to support 256 colors
export TERM=xterm-256color
mvn spring-boot:run -Pcolor
```

## 💡 Best Practices

### **Development Environment**
```bash
# Use color profile for development
mvn spring-boot:run -Pcolor
```

### **Production Environment**
```bash
# Disable colors for production logs
mvn spring-boot:run -Dspring.output.ansi.enabled=never
```

### **CI/CD Pipelines**
```bash
# Auto-detect color support
mvn spring-boot:run -Dspring.output.ansi.enabled=detect
```

## 🎯 Testing Your Colorized Setup

1. **Start with colors:**
   ```bash
   mvn spring-boot:run -Pcolor
   ```

2. **Make a test request:**
   ```bash
   curl -X POST http://localhost:8085/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email": "john.doe@example.com", "password": "password123"}'
   ```

3. **Observe colorized logs:**
   - **Security filters**: Green DEBUG messages
   - **Controller mapping**: Blue INFO messages  
   - **SQL queries**: Green DEBUG with syntax highlighting
   - **Errors**: Red ERROR messages

## 📋 Quick Reference

| Command | Purpose |
|---------|---------|
| `mvn spring-boot:run -Pcolor` | **Recommended**: Use color profile |
| `mvn spring-boot:run -Dspring-boot.run.jvmArguments='-Dspring.output.ansi.enabled=always'` | Simple color enable |
| `mvn -Djansi.force=true spring-boot:run` | Force Maven colors |
| `tail -f app.log` | View file logs (no colors) |
| `tail -f app.log \| ccze -A` | Colorize file logs with ccze |

## 🎉 Result

Your Spring Boot application now runs with beautiful colorized output making it much easier to:
- **Spot errors** (red)
- **Track request flow** (different colors for each layer)
- **Debug issues** (green debug messages)
- **Monitor SQL** (highlighted syntax)
- **See security processing** (color-coded filters)

**Enjoy your colorful Spring Boot development experience! 🌈**
