CC = gcc

CFLAGS = -Wall -Wextra -std=c11

LDFLAGS = -pthread

TARGET = main

# Regras
all: $(TARGET)

$(TARGET): main.c
	$(CC) $(CFLAGS) $< -o $@ $(LDFLAGS)

run: $(TARGET)
	./$(TARGET)

clean:
	rm -f $(TARGET) *.o

