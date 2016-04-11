package de.mkrnr.rse.train;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import de.mkrnr.rse.util.Configuration;

public abstract class ConfigurableBuilder {
    protected List<Configuration> configurations;

    public ConfigurableBuilder(List<Configuration> configurations) {
        this.configurations = configurations;
    }

    public List<Configuration> getConfigurations() {
        return this.configurations;
    }

    public List<String> getPossibleConfigurations() {
        List<String> possibleConfigurations = new ArrayList<String>();
        Field[] fields = CRFTrainerByLabelLikelihoodBuilder.class.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isProtected(field.getModifiers()) && Modifier.isStatic(field.getModifiers())
                    && Modifier.isFinal(field.getModifiers()) && field.getType().equals(String.class)) {
                try {
                    possibleConfigurations.add((String) field.get(""));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return possibleConfigurations;
    }

}
