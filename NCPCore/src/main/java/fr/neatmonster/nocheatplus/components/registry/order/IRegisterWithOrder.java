package fr.neatmonster.nocheatplus.components.registry.order;

public interface IRegisterWithOrder {
    
     public RegistrationOrder getRegistrationOrder(Class<?> registerForType);
    // TODO: getRegistrationOrder(Class<?> registerForType, Class<?> registryType) ?
}
