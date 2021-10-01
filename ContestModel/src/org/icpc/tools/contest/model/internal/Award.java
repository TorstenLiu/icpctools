package org.icpc.tools.contest.model.internal;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.icpc.tools.contest.model.IAward;
import org.icpc.tools.contest.model.IContest;
import org.icpc.tools.contest.model.feed.JSONEncoder;
import org.icpc.tools.contest.model.feed.JSONParser;

public class Award extends ContestObject implements IAward {
	public static final String CITATION = "citation";
	public static final String TEAM_IDS = "team_ids";
	public static final String SHOW = "show";
	public static final String DISPLAY_MODE = "display_mode";
	public static final String PARAMETER = "parameter";

	private String[] teamIds;
	private DisplayMode mode;
	private String citation;
	private String parameter;

	public Award() {
		// create an empty award
	}

	public Award(AwardType type, String teamId, String citation) {
		this(type.getPattern(""), new String[] { teamId }, citation);
	}

	public Award(AwardType type, String id, String[] teamIds, String citation) {
		this(type.getPattern(id), teamIds, citation);
	}

	private Award(String id, String[] teamIds, String citation) {
		this(id, teamIds, citation, null);
	}

	public Award(AwardType type, String teamId, String citation, DisplayMode mode) {
		this(type.getPattern(""), new String[] { teamId }, citation, mode);
	}

	public Award(AwardType type, String id, String[] teamIds, String citation, DisplayMode mode) {
		this(type.getPattern(id), teamIds, citation, mode);
	}

	private Award(String id, String[] teamIds, String citation, DisplayMode mode) {
		super(id);
		this.teamIds = teamIds;
		this.citation = citation;
		this.mode = mode;
	}

	@Override
	public ContestType getType() {
		return ContestType.AWARD;
	}

	@Override
	public String[] getTeamIds() {
		return teamIds;
	}

	public void setTeamIds(String[] teamIds) {
		this.teamIds = teamIds;
	}

	@Override
	public AwardType getAwardType() {
		for (AwardType at : KNOWN_TYPES) {
			if (Pattern.matches(at.getRexEx(), id))
				return at;
		}
		return OTHER;
	}

	@Override
	public String getCitation() {
		return citation;
	}

	public void setCitation(String citation) {
		this.citation = citation;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	@Override
	public boolean hasDisplayMode() {
		return mode != null;
	}

	@Override
	public DisplayMode getDisplayMode() {
		if (mode == null)
			return DisplayMode.DETAIL;
		return mode;
	}

	public void setDisplayMode(DisplayMode s) {
		this.mode = s;
	}

	@Override
	public String getParameter() {
		return parameter;
	}

	@Override
	protected boolean addImpl(String name, Object value) throws Exception {
		if (TEAM_IDS.equals(name)) {
			if (value == null || "null".equals(value))
				teamIds = null;
			else {
				Object[] ob = JSONParser.getOrReadArray(value);
				teamIds = new String[ob.length];
				for (int i = 0; i < ob.length; i++)
					teamIds[i] = (String) ob[i];
			}
			return true;
		} else if (name.equals(CITATION)) {
			citation = (String) value;
			return true;
		} else if (name.equals(SHOW)) {
			if (parseBoolean(value))
				mode = DisplayMode.DETAIL;
			else
				mode = DisplayMode.PAUSE;
			return true;
		} else if (name.equals(DISPLAY_MODE)) {
			if ("detail".equals(value))
				mode = DisplayMode.DETAIL;
			else if ("pause".equals(value))
				mode = DisplayMode.PAUSE;
			else if ("list".equals(value))
				mode = DisplayMode.LIST;
			else if ("ignore".equals(value))
				mode = DisplayMode.IGNORE;

			return true;
		} else if (name.equals(PARAMETER)) {
			parameter = (String) value;
			return true;
		}

		return false;
	}

	@Override
	protected void getPropertiesImpl(Map<String, Object> props) {
		super.getPropertiesImpl(props);
		props.put(CITATION, citation);
		if (teamIds != null) {
			if (teamIds.length == 0)
				props.put(TEAM_IDS, "[]");
			else
				props.put(TEAM_IDS, "[\"" + String.join("\",\"", teamIds) + "\"]");
		}
		if (mode != null && mode != DisplayMode.DETAIL)
			props.put(DISPLAY_MODE, mode.name().toLowerCase());
		if (parameter != null)
			props.put(PARAMETER, parameter);
	}

	@Override
	public void writeBody(JSONEncoder je) {
		je.encode(ID, id);
		if (citation != null)
			je.encode(CITATION, citation);
		if (teamIds != null) {
			if (teamIds.length == 0)
				je.encodePrimitive(TEAM_IDS, "[]");
			else
				je.encodePrimitive(TEAM_IDS, "[\"" + String.join("\",\"", teamIds) + "\"]");
		}
		if (mode != null && mode != DisplayMode.DETAIL)
			je.encode(DISPLAY_MODE, mode.name().toLowerCase());
		if (parameter != null)
			je.encode(PARAMETER, parameter);
	}

	@Override
	public List<String> validate(IContest c) {
		List<String> errors = super.validate(c);

		if (citation == null || citation.isEmpty())
			errors.add("Citation missing");

		if (teamIds != null && teamIds.length > 0) {
			for (String tId : teamIds) {
				if (c.getTeamById(tId) == null)
					errors.add("Invalid team " + tId);
			}
		}

		if (errors.isEmpty())
			return null;
		return errors;
	}
}