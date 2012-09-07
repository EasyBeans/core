/**
 * EasyBeans
 * Copyright (C) 2012 Bull S.A.S.
 * Contact: easybeans@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * --------------------------------------------------------------------------
 * $Id$
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.quartz;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.ScheduleExpression;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerHandle;
import javax.ejb.TimerService;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.EZBServer;
import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.FactoryException;
import org.ow2.easybeans.component.api.EZBComponentException;
import org.ow2.easybeans.component.itf.TimerComponent;
import org.ow2.easybeans.component.util.Property;
import org.ow2.easybeans.container.JContainer3;
import org.ow2.easybeans.container.session.stateless.StatelessSessionFactory;
import org.ow2.easybeans.server.Embedded;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.archive.impl.MemoryArchive;
import org.ow2.util.ee.deploy.api.deployable.IDeployable;
import org.ow2.util.ee.deploy.api.helper.DeployableHelperException;
import org.ow2.util.ee.deploy.impl.helper.DeployableHelper;
import org.ow2.util.marshalling.Serialization;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests the Quartz Timer service.
 * @author Florent Benoit
 */
public class TestQuartzTimerService {


    private static final long MILLIS = 1000L;
    private static final long TWO_MINUTES = 2 * 60 * 1000L;
    private static final long THREE_MINUTES = 3 * 60 * 1000L;



    TimerService timerService = null;

    @BeforeClass
    protected void init() throws FactoryException, DeployableHelperException, EZBComponentException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        QuartzComponent quartzComponent = new QuartzComponent();

        List<Property> properties = new ArrayList<Property>();

        Property p1 = new Property();
        p1.setName("org.quartz.scheduler.instanceName");
        p1.setValue("EasyBeans");
        properties.add(p1);

        Property p2 = new Property();
        p2.setName("org.quartz.threadPool.class");
        p2.setValue("org.quartz.simpl.SimpleThreadPool");
        properties.add(p2);

        Property p3 = new Property();
        p3.setName("org.quartz.threadPool.threadCount");
        p3.setValue("5");
        properties.add(p3);

        Property p4 = new Property();
        p4.setName("org.quartz.threadPool.threadPriority");
        p4.setValue("5");
        properties.add(p4);

        Property p5 = new Property();
        p5.setName("org.quartz.jobStore.class");
        p5.setValue("org.quartz.simpl.RAMJobStore");
        properties.add(p5);

        quartzComponent.setProperties(properties);
        quartzComponent.init();
        quartzComponent.start();

        List<String> classes = new ArrayList<String>();
        classes.add(DummyStateless.class.getName());
        IArchive inMemoryArchive = new MemoryArchive(TestQuartzTimerService.class.getClassLoader(), classes);

        IDeployable<?> deployable = DeployableHelper.getDeployable(inMemoryArchive);

        EZBServer easybeans = new Embedded();
        EZBContainer container = easybeans.createContainer(deployable);

        container.setClassLoader(TestQuartzTimerService.class.getClassLoader());
        StatelessSessionFactory statelessSessionFactory = new StatelessSessionFactory(DummyStateless.class.getName(), container);

        Field factoriesField = JContainer3.class.getDeclaredField("factories");
        factoriesField.setAccessible(true);
        Map<String, Factory<?, ?>> factories = (Map<String, Factory<?, ?>>) factoriesField.get(container);
        factories.put(statelessSessionFactory.getClassName(), statelessSessionFactory);

        this.timerService = quartzComponent.getTimerService(statelessSessionFactory);

        easybeans.getComponentManager().getComponentRegistry().register(TimerComponent.class.getName(), quartzComponent);

    }

    @Test
    public void testSimpleTimer() {
        String serializableData = "serializable";
        long TEN_SECONDS = 10000L;
        EasyBeansTimer timer = (EasyBeansTimer) this.timerService.createTimer(new Date(System.currentTimeMillis() + TEN_SECONDS), serializableData);

        // Check Serializable stuff
        Assert.assertEquals(serializableData, timer.getInfo());

        // We should get the fire in less than 10s
        Assert.assertTrue(timer.getTimeRemaining() < TEN_SECONDS);
    }


    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testInvalidDateSingleTimer() {

        TimerConfig timerConfig = new TimerConfig();

        // negative value
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1945);

        this.timerService.createSingleActionTimer(calendar.getTime(), timerConfig);
        Assert.fail("IllegalArgumentException should be thrown");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testInvalidLongSingleTimer() {

        TimerConfig timerConfig = new TimerConfig();


        this.timerService.createSingleActionTimer(-1L, timerConfig);
        Assert.fail("IllegalArgumentException should be thrown");
    }


    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testNullDateSingleTimer() {
        this.timerService.createSingleActionTimer(null, new TimerConfig());
        Assert.fail("IllegalArgumentException should be thrown");
    }




    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testInvalidDateIntervalTimer() {

        TimerConfig timerConfig = new TimerConfig();

        // negative value
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1945);

        this.timerService.createIntervalTimer(calendar.getTime(), 1L, timerConfig);
        Assert.fail("IllegalArgumentException should be thrown");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testInvalidLongIntervalTimer() {

        TimerConfig timerConfig = new TimerConfig();


        this.timerService.createIntervalTimer(-1L, 1L, timerConfig);
        Assert.fail("IllegalArgumentException should be thrown");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testInvalidNegativeIntervalLongIntervalTimer() {

        TimerConfig timerConfig = new TimerConfig();


        this.timerService.createIntervalTimer(new Date(), -1L, timerConfig);
        Assert.fail("IllegalArgumentException should be thrown");
    }

    /**
     * Test with an end time of the schedule expression.
     */
    @Test(expectedExceptions=NoSuchObjectLocalException.class)
    public void testAfterExpiration() {

        Calendar now = new GregorianCalendar();
        Calendar expressionDate = (Calendar) now.clone();
        expressionDate.add(Calendar.YEAR, 30);

        Calendar endDate = (Calendar) expressionDate.clone();
        endDate.add(Calendar.YEAR, -1);

        ScheduleExpression scheduleExpression = new ScheduleExpression().second(expressionDate.get(Calendar.SECOND)).minute(expressionDate.get(Calendar.MINUTE)).hour(expressionDate.get(Calendar.HOUR_OF_DAY)).month(expressionDate.get(Calendar.MONTH)).year(expressionDate.get(Calendar.YEAR)).end(endDate.getTime());
        EasyBeansTimer timer = (EasyBeansTimer) this.timerService.createCalendarTimer(scheduleExpression);
        Date fireTime = timer.getTrigger().getNextFireTime();
        Assert.assertNull(fireTime);

        // Should fail
        timer.getNextTimeout();
        Assert.fail("getNextTimeout method should have failed");
    }


    /**
     * Test with an end time < start time.
     * So all methods on the timer will give errors
     */
    @Test(expectedExceptions=NoSuchObjectLocalException.class)
    public void testEndBeforeStart() {

        Calendar now = new GregorianCalendar();

        Calendar startDate = (Calendar) now.clone();
        startDate.add(Calendar.MINUTE, 300);

        Calendar endDate = (Calendar) now.clone();
        endDate.add(Calendar.MINUTE, 50);

        now.add(Calendar.SECOND, 30);

        ScheduleExpression scheduleExpression = new ScheduleExpression().second(now.get(Calendar.SECOND)).minute(now.get(Calendar.MINUTE)).hour(now.get(Calendar.HOUR_OF_DAY)).start(startDate.getTime()).end(endDate.getTime());
        EasyBeansTimer timer = (EasyBeansTimer) this.timerService.createCalendarTimer(scheduleExpression);
        Date fireTime = timer.getTrigger().getNextFireTime();
        Assert.assertNull(fireTime);

        // Should fail
        timer.getNextTimeout();
        Assert.fail("getNextTimeout method should have failed");
    }



    /**
    * Check that the expected next timeout is valid
    */
   @Test
   public void testDayOfWeekNextTimeout() {

       // Now
       Calendar now = Calendar.getInstance();
       now.set(Calendar.MILLISECOND, 0);

       for (int i = 0; i < 7; i++) {

           int seconds = now.get(Calendar.SECOND);
           int minutes = now.get(Calendar.MINUTE);
           int hour =now.get(Calendar.HOUR_OF_DAY);
           int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
           ScheduleExpression scheduleExpression = new ScheduleExpression().second(seconds).minute(minutes).hour(hour).dayOfMonth("*").month("*").year("*").dayOfWeek(dayOfWeek);

           // add a new day
           now.add(Calendar.DAY_OF_WEEK, 1);

           EasyBeansTimer timer = (EasyBeansTimer) this.timerService.createCalendarTimer(scheduleExpression);
           Date nextTimeout = timer.getNextTimeout();

           Assert.assertNotNull(timer);
           Assert.assertNotNull(nextTimeout);

           // Check that next timeout is for the given time
           Assert.assertEquals(nextTimeout, now.getTime());

       }


   }



    @Test
    public void testScheduleWithStartAfterFirstDate() {

        // Now
        Calendar now = Calendar.getInstance();
        now.add(Calendar.SECOND, 20);
        now.add(Calendar.YEAR, 4);


        int seconds = now.get(Calendar.SECOND);
        int minutes = now.get(Calendar.MINUTE);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int month = now.get(Calendar.MONTH) + 1;
        int dayOfMonth = now.get(Calendar.DAY_OF_MONTH);

        Calendar startDate = (Calendar) now.clone();
        startDate.add(Calendar.SECOND, 1);


        ScheduleExpression scheduleExpression = new ScheduleExpression().second(seconds).minute(minutes).hour(hour).dayOfMonth(dayOfMonth).month(month).year("2012-2030").start(startDate.getTime());

        EasyBeansTimer timer = (EasyBeansTimer) this.timerService.createCalendarTimer(scheduleExpression);
        Date fireTime = timer.getTrigger().getNextFireTime();
        Date nextTimeout = timer.getNextTimeout();
        Calendar nextTimeoutCalendar = Calendar.getInstance();
        nextTimeoutCalendar.setTime(nextTimeout);


        Assert.assertNotNull(fireTime);
        Assert.assertEquals( nextTimeoutCalendar.get(Calendar.YEAR), now.get(Calendar.YEAR) + 1);

    }




    @Test
    public void testScheduleExpression() {

        Date now = new Date();
        int currentMinutes = Calendar.getInstance().get(Calendar.MINUTE);
        currentMinutes +=3;
        if (currentMinutes > 59) {
            currentMinutes -=60;
        }

        ScheduleExpression scheduleExpression = new ScheduleExpression().second("0").minute(currentMinutes + "/5").hour("*");
        EasyBeansTimer timer = (EasyBeansTimer) this.timerService.createCalendarTimer(scheduleExpression);
        Date fireTime = timer.getTrigger().getNextFireTime();
        Assert.assertTrue(fireTime.getTime() - now.getTime() > TWO_MINUTES);
        Assert.assertTrue(fireTime.getTime() - now.getTime() < THREE_MINUTES);
        Assert.assertEquals(fireTime.getSeconds(), 0);
    }


    @Test
    public void testTimerEquals() {

        ScheduleExpression scheduleExpression = new ScheduleExpression().second("*").minute("*").hour("*").year(2050);
        TimerConfig timerConfig = new TimerConfig();
        timerConfig.setInfo("testTimerEquals");

        EasyBeansTimer timer = (EasyBeansTimer) this.timerService.createCalendarTimer(scheduleExpression, timerConfig);


        // We should be equals to ourself
        Assert.assertEquals(timer, timer);


        Collection<Timer> timers = this.timerService.getTimers();
        Assert.assertNotNull(timers);

        Assert.assertTrue(timers.size() > 0);

        Timer foundTimer = null;
        boolean found = false;
        for (Timer currentTimer : timers) {
            if ("testTimerEquals".equals(currentTimer.getInfo())) {
                found = true;
                foundTimer = currentTimer;
                break;
            }
        }
        Assert.assertTrue(found);



        Assert.assertNotNull(foundTimer);
        // We should be equals to the timer retrieved
        Assert.assertEquals(timer, foundTimer);
    }


    @Test
    public void testTimerHandle() throws IOException, ClassNotFoundException {

        ScheduleExpression scheduleExpression = new ScheduleExpression().second("*").minute("*").hour("*").year(2050);
        EasyBeansTimer timer = (EasyBeansTimer) this.timerService.createCalendarTimer(scheduleExpression);

        TimerHandle timerHandle = timer.getHandle();
        byte[] bytesHandle = Serialization.storeObject(timerHandle);
        TimerHandle reconstructHandle = (TimerHandle) Serialization.loadObject(bytesHandle);

        Timer reconstructTimer = reconstructHandle.getTimer();

        // We should be equals to the timer retrieved
        Assert.assertEquals(timer, reconstructTimer);
    }

}
