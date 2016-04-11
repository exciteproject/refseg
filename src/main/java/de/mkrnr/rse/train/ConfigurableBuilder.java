package de.mkrnr.rse.train;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public abstract class ConfigurableBuilder {
    protected Configurations configurations;

    public ConfigurableBuilder() {
        this.configurations = new Configurations();
    }

    public Configurations getConfigurations() {
        return this.configurations;
    }

    public List<String> getPossibleConfigurations() {
        List<String> possibleConfigurations = new ArrayList<String>();
        Field[] fields = CRFTrainerByLabelLikelihoodBuilder.class.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())
                    && field.getType().equals(String.class)) {
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
