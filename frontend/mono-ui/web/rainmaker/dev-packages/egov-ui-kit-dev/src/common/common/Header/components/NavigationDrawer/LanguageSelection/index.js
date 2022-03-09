import React, { Component } from "react";
import { ButtonGroup, Icon } from "components";
import { connect } from "react-redux";
import get from "lodash/get";
import { getLocale, getTenantId,  getUserInfo, setStoredModulesList } from "egov-ui-kit/utils/localStorageUtils";
import Label from "egov-ui-kit/utils/translationNode";
import { getQueryArg } from "egov-ui-kit/utils/commons";
import { setLocalizationLabels } from "egov-ui-kit/redux/app/actions";
class LanguageSelection extends Component {
  state = {
    value: getLocale(),
  };

  onClick = (value) => {
    this.setState({ value });
    let tenantId = getTenantId();

    if (process.env.REACT_APP_NAME === "Citizen") {
      const tenantInfo=getQueryArg(window.location.href, "tenantId")
      const userInfo = JSON.parse(getUserInfo());
      tenantId = userInfo && userInfo.permanentCity;
      tenantId = tenantInfo?tenantInfo:tenantId;
    }
    var resetList=[];
    var newList =JSON.stringify(resetList);
    setStoredModulesList(newList);
    let locale= getLocale();
    let resultArray=[];
    setLocalizationLabels(locale, resultArray);
    this.props.fetchLocalizationLabel(value, tenantId, tenantId);
  };

  styles = {
    selectedLabelStyle: {
      color: "#ffffff",
    },
    selectedStyle: {
      backgroundColor: "#fe7a51",
      border: "1px solid #fe7a51",
    },
    defaultStyle: {
      border: "1px solid #fe7a51",
      borderRadius: "1px",
      marginRight: "4.65%",
      height: "30px",
      lineHeight: "30px",
      width: "28.48%",
      minWidth: "inherit",
      padding: 0,
    },
    defaultLabelStyle: {
      textTransform: "none",
      fontWeight: "500",
      color: "#484848",
      verticalAlign: "initial",
      padding: 0,
    },
  };

  render() {
    const { styles, onClick } = this;
    const { languages } = this.props;
    const { value } = this.state;
    return (
      // <div className="drawer-button-toggle-container">
      <div className="rainmaker-displayInline" style={{ marginTop: 10, marginLeft: 4 }}>
        <Icon style={{ height: "24px", width: "21px" }} action="action" name="language" />
        <div style={{ marginLeft: 12 , width: "100%"  }}>
          <Label
            className="menuStyle with-childs"
            containerStyle={{ marginLeft: 0, marginBottom: 10, marginRight: 80 }}
            label="ACTION_TEST_LANGUAGE"
            color="rgba(0, 0, 0, 0.87)"
          />

          <ButtonGroup
            items={languages}
            onClick={onClick}
            selected={value}
            defaultStyle={styles.defaultStyle}
            defaultLabelStyle={styles.defaultLabelStyle}
            selectedStyle={styles.selectedStyle}
            selectedLabelStyle={styles.selectedLabelStyle}
            multiple={false}
          />
        </div>
      </div>
    );
  }
}

const mapStateToProps = ({ common }) => {
  const { stateInfoById } = common;
  let languages = get(stateInfoById, "0.languages", []);
  return { languages };
};

export default connect(
  mapStateToProps,
  null
)(LanguageSelection);
