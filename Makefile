SHELL := /bin/bash

.PHONY: help baseline conduit-build loan-verify

help:
	@echo "make baseline        Build and test both apps on the active JDK (conduit: ./gradlew build, loan: ./mvnw verify)"
	@echo "make conduit-build   ./gradlew build in conduit-api/"
	@echo "make loan-verify     ./mvnw verify in loan-service/"

baseline: conduit-build loan-verify

conduit-build:
	cd conduit-api && ./gradlew --no-daemon clean build

loan-verify:
	cd loan-service && ./mvnw -B clean verify
