package com.cheatdatabase.adapters

import com.cheatdatabase.data.model.Cheat


class MyUnpublishedCheatDataAdapter {
//class MyUnpublishedCheatDataAdapter : RecyclerView.Adapter<MyUnpublishedCheatDataAdapter.UnpublishedCheatViewHolder>() {

    var cheats: ArrayList<Cheat>? = null

//    @NonNull
//    override fun onCreateViewHolder(@NonNull viewGroup: ViewGroup, i: Int): UnpublishedCheatViewHolder {
//        val employeeListItemBinding: UnpublishedCheatListItemBinding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.context), R.layout.listrow_unpublished_cheat_item, viewGroup, false)
//        return UnpublishedCheatViewHolder(employeeListItemBinding)
//    }
//
//    override fun onBindViewHolder(employeeViewHolder: UnpublishedCheatViewHolder, i: Int) {
//        val currentStudent: Cheat = cheats!![i]
//        employeeViewHolder.employeeListItemBinding.setEmployee(currentStudent)
//    }
//
//    override fun getItemCount(): Int {
//        return cheats?.size ?: 0
//    }
//
//    fun setEmployeeList(employees: ArrayList<Cheat>) {
//        this.cheats = employees
//        notifyDataSetChanged()
//    }
//
//    class UnpublishedCheatViewHolder(employeetListItemBinding: UnpublishedCheatListItemBinding) : RecyclerView.ViewHolder(employeetListItemBinding.getRoot()) {
//        val employeeListItemBinding: UnpublishedCheatListItemBinding
//
//        init {
//            employeeListItemBinding = employeetListItemBinding
//        }
//    }
}