SRC = $(shell find . -name "*.java")
CLASS = $(SRC:.java=.class)

all: $(CLASS)

$(CLASS): $(SRC)
	javac $(SRC)

.PHONY: clean
clean:
	find . -name "*.class" -delete

.PHONY: server
server: $(CLASS)
	java Server 8080

.PHONY: client
client: $(CLASS)
	java Client localhost 8080

.PHONY: clear_db
clear_db:
	rm database.csv
