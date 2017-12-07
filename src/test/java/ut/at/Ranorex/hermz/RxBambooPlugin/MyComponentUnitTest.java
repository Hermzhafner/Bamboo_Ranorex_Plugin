package ut.at.Ranorex.hermz.RxBambooPlugin;

import org.junit.Test;
import at.Ranorex.hermz.RxBambooPlugin.api.MyPluginComponent;
import at.Ranorex.hermz.RxBambooPlugin.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        //assertEquals("names do not match!", "myComponent",component.getName());
    }
}