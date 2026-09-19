package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"io"
	"log"
	"net/http"
	"time"
)

type GitHubRelease struct {
	TagName string `json:"tag_name"`
}

var client = &http.Client{Timeout: 10 * time.Second}

var latestVersion = "1.0.0"

func main() {
	port := flag.Int("port", 8080, "port to listen on")
	flag.Parse()

	go func() {
		http.HandleFunc("/", getVersionHandler)
		log.Printf("Server starting on port %d", *port)
		log.Fatal(http.ListenAndServe(fmt.Sprintf(":%d", *port), nil))
	}()

	// Schedule fetch from GitHub every hour
	ticker := time.NewTicker(1 * time.Hour)
	defer ticker.Stop()
	go fetchReleaseInfo()
	select {}
}

func fetchReleaseInfo() {
	req, err := http.NewRequest("GET", "https://api.github.com/repos/ingStudiosOfficial/turtlebrowse/releases/latest", nil)
	if err != nil {
		log.Printf("Failed to create request: %v", err)
		return
	}

	req.Header.Set("User-Agent", "Turtlebrowse-Update-Proxy")

	resp, err := client.Do(req)
	if err != nil {
		log.Printf("Failed to fetch release details: %v", err)
		return
	}

	if resp.StatusCode != http.StatusOK {
		log.Printf("Status code: %v", resp.StatusCode)
		return
	}

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		log.Printf("Failed to read body: %v", body)
		return
	}

	var release GitHubRelease
	if err := json.Unmarshal(body, &release); err != nil {
		log.Printf("Failed to parse JSON: %v", err)
		return
	}

	latestVersion = release.TagName

	log.Printf("Successfully set latest version: %v", latestVersion)
}

func getVersionHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	w.Header().Set("Content-Type", "text/plain")
	
	fmt.Fprint(w, latestVersion)
}