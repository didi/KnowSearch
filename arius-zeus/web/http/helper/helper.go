package helper

func StatusClass(status string) string {
	if status == "running" || status == "waiting" {
		return ""
	}

	if status == "success" {
		return "status-success"
	}

	return "status-other"
}
