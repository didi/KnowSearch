import { Select, Spin } from "antd";
import debounce from "lodash/debounce";
import React from "react";
import { getCookie } from "lib/utils";

export const UserSelect = (props: any) => {
  const { fetchOptions, setOwnerIdList, targetKeys, ownersList, isNew, list } = props;

  const [fetching, setFetching] = React.useState(false);
  const [options, setOptions] = React.useState<any[]>(ownersList || []);
  const [value, setValue] = React.useState(props.value || []);

  const fetchRef = React.useRef(0);

  React.useEffect(() => {
    if (isNew) {
      let option = {
        label: getCookie("userName"),
        value: Number(getCookie("userId")),
      };
      setValue([Number(getCookie("userId"))]);
      setOptions([option]);
    }
  }, [list]);

  React.useEffect(() => {
    let list = props?.list?.filter((item) => targetKeys?.includes(item.key));
    let options = (list || []).map((item) => {
      return {
        label: item.label,
        value: item.key,
      };
    });
    let newValue = value.filter((item) => targetKeys?.includes(item));
    setOptions(options);
    setValue(newValue);
    setOwnerIdList(newValue);
  }, [targetKeys]);

  const debounceFetcher = React.useMemo(() => {
    const loadOptions = (value: string) => {
      fetchRef.current += 1;
      const fetchId = fetchRef.current;
      setOptions([]);
      setFetching(true);

      fetchOptions(value).then((res) => {
        if (fetchId !== fetchRef.current) {
          // for fetch callback order
          return;
        }
        const newOptions = (res || []).map((item) => ({
          label: item.userName,
          value: item.id,
        }));
        setOptions(newOptions);
        setFetching(false);
      });
    };

    return debounce(loadOptions, 800);
  }, [fetchOptions]);

  const handleChange = (values) => {
    setValue(values);
    setOwnerIdList(values);
    props.onChange && props.onChange(values);
  };

  return (
    <>
      <Select
        value={value}
        filterOption={(val, option) => {
          let label = `${option?.label}`;
          return label?.includes(val);
        }}
        notFoundContent={fetching ? <Spin size="small" /> : null}
        onChange={handleChange}
        options={options}
        mode={props.mode}
        placeholder={props.placeholder || ""}
      />
    </>
  );
};
