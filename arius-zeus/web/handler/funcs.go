package handler

import (
	"encoding/json"
	"io/ioutil"
	"net/http"
)

func JsonBind(r *http.Request, f interface{}) error {
	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		return err
	}

	err = json.Unmarshal(body, &f)
	return err
}
