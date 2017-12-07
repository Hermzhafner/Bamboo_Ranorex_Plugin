package at.Ranorex.hermz.RxBambooPlugin.impl;

import java.util.Map;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.util.concurrent.NotNull;
import com.atlassian.util.concurrent.Nullable;

public class RxTaskConfigurator extends AbstractTaskConfigurator {
	
	public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition)
	{
	    final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

	    config.put("rxparams", params.getString("Parameter"));

	    return config;
	}
	
	@Override
	public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
	{
	    super.populateContextForEdit(context, taskDefinition);

	    context.put("Parameter", taskDefinition.getConfiguration().get("rxparams"));
	}

}
