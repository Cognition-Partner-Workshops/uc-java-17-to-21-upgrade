SHELL := /bin/bash

.PHONY: help baseline conduit-build loan-verify parity parity-record perf

help:
	@echo "make baseline        Build and test both apps on the active JDK (conduit: ./gradlew build, loan: ./mvnw verify)"
	@echo "make conduit-build   ./gradlew build in conduit-api/"
	@echo "make loan-verify     ./mvnw verify in loan-service/"
	@echo "make parity APP=conduit|loan [TARGET=url]   Compare HTTP behaviour with parity/<app>/transcripts (non-zero on diff)"
	@echo "make perf APP=conduit|loan [TARGET=url]     Latency of the fan-out endpoint (perf/latency.py)"

baseline: conduit-build loan-verify

conduit-build:
	cd conduit-api && ./gradlew --no-daemon clean build

loan-verify:
	cd loan-service && ./mvnw -B clean verify

APP ?=
TARGET ?=

parity:
	@test -n "$(APP)" || (echo "usage: make parity APP=conduit|loan [TARGET=url]" && exit 2)
	parity/run.sh $(APP) $(TARGET)

parity-record:
	@test -n "$(APP)" || (echo "usage: make parity-record APP=conduit|loan" && exit 2)
	parity/run.sh $(APP) --record

perf:
	@test -n "$(APP)" || (echo "usage: make perf APP=conduit|loan [TARGET=url]" && exit 2)
	perf/run.sh $(APP) $(TARGET)
