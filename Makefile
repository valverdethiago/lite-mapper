# Variables
MODULE_BENCHMARK=lite-mapper-mapstruct-benchmark
BENCHMARK_JAR=$(MODULE_BENCHMARK)/target/$(MODULE_BENCHMARK)-1.0-SNAPSHOT.jar
MAVEN=mvn
JAVA=java

# Default target
.PHONY: all
all: build run

# Build the project
.PHONY: build
build:
	$(MAVEN) clean package -pl $(MODULE_BENCHMARK)

# Run the benchmark
.PHONY: run
run: $(BENCHMARK_JAR)
	$(JAVA) -jar $(BENCHMARK_JAR)

# Clean the project
.PHONY: clean
clean:
	$(MAVEN) clean
